package frc.robot.subsystems

import com.ctre.phoenix6.Utils
import com.ctre.phoenix6.swerve.SwerveModule
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType.Velocity
import com.ctre.phoenix6.swerve.SwerveRequest
import frc.robot.OI
import frc.robot.generated.TunerConstants
import frc.robot.lib.inches
import frc.robot.lib.joystickScale
import frc.robot.lib.meters
import frc.robot.lib.metersPerSecond
import frc.robot.lib.milliseconds
import frc.robot.lib.radians
import frc.robot.subsystems.SwerveDrivetrain.SwerveState.*
import kotlin.math.abs
import kotlin.math.sign
import org.littletonrobotics.junction.Logger
import org.wpilib.driverstation.Alliance
import org.wpilib.driverstation.MatchState
import org.wpilib.driverstation.RobotState
import org.wpilib.math.controller.PIDController
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.geometry.Rotation2d
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.math.util.MathSharedStore
import org.wpilib.math.util.MathUtil
import org.wpilib.system.Notifier
import org.wpilib.system.RobotController
import org.wpilib.system.Timer
import org.wpilib.units.measure.Angle
import org.wpilib.units.measure.Distance

object SwerveDrivetrain :
    TunerConstants.TunerSwerveDrivetrain(
        TunerConstants.DrivetrainConstants,
        TunerConstants.FrontLeft,
        TunerConstants.FrontRight,
        TunerConstants.BackLeft,
        TunerConstants.BackRight,
    ) {

    enum class SwerveState {
        FieldCentric,
        XBrake,
    }

    var currentState = FieldCentric

    // Speeds are in SI units
    val MAX_SPEED = TunerConstants.kSpeedAt12Volts.metersPerSecond
    const val MAX_ROT_SPEED = 2 * Math.PI

    private val fieldCentricRequest =
        SwerveRequest.FieldCentric()
            .withDriveRequestType(Velocity)
            .withSteerRequestType(SwerveModule.SteerRequestType.Position)
            .withDeadband(0.05 * MAX_SPEED)
            .withRotationalDeadband(0.05 * MAX_ROT_SPEED)

    private val xBrakeRequest = SwerveRequest.SwerveDriveBrake()
    private val idleRequest = SwerveRequest.Idle()

    // Heading that FieldCentric holds after the driver releases the rotation stick
    private val headingTimer = Timer().apply { start() }
    private var oldHeading = 0.0

    fun stateActions() {
        if (currentState != FieldCentric) {
            headingTimer.reset()
        }
        when (currentState) {
            FieldCentric -> {
                var rot = OI.rotInput() * MAX_ROT_SPEED
                if (rot == 0.0 && headingTimer.hasElapsed(350.milliseconds)) {
                    rot = headingPIDController.calculate(state.Pose.rotation.radians, oldHeading)
                } else if (rot != 0.0) {
                    headingTimer.reset()
                } else {
                    oldHeading = state.Pose.rotation.radians
                }
                val translation = OI.translationInput() * MAX_SPEED
                val scale =
                    kinematics.joystickScale(
                        ChassisVelocities(translation.x, translation.y, rot)
                            .toRobotRelative(state.Pose.rotation - operatorForwardDirection),
                        MAX_SPEED,
                        MAX_ROT_SPEED,
                    )
                val limited = translationLimiter.calculate(translation * scale)
                setControl(
                    fieldCentricRequest
                        .withVelocityX(limited.x)
                        .withVelocityY(limited.y)
                        .withRotationalRate(rot * scale)
                )
            }

            XBrake -> brake()
        }
    }

    fun stateTransitions() {
        if (OI.resetRobotYaw()) {
            seedFieldCentric()
            headingTimer.reset()
        }
        when (currentState) {
            FieldCentric -> {
                if (OI.xBrake()) currentState = XBrake
            }

            XBrake -> {
                if (!OI.xBrake()) currentState = FieldCentric
            }
        }
    }

    fun brake() {
        setControl(xBrakeRequest)
    }

    fun idle() {
        setControl(idleRequest)
    }

    private val posePIDController = PIDController(5.0, 0.0, 0.5)
    private val headingPIDController =
        PIDController(5.0, 0.0, 0.15).apply { enableContinuousInput(-Math.PI, Math.PI) }

    private val translationLimiter = TranslationSlewRateLimiter(15.0)

    private val driveToPoseRequest =
        SwerveRequest.FieldCentricFacingAngle()
            .withHeadingPID(5.0, 0.0, 0.15)
            .withRotationalDeadband(0.05)
            .withForwardPerspective(SwerveRequest.ForwardPerspectiveValue.BlueAlliance)
            .withDriveRequestType(Velocity)
            .withSteerRequestType(SwerveModule.SteerRequestType.Position)

    private val rotateAroundPointRequest =
        SwerveRequest.RobotCentric().withDriveRequestType(Velocity)

    /**
     * Drives to a pose using a PID controller. Call this one time each loop until it returns true.
     *
     * @param target the pose to drive to, in blue-origin field coordinates
     * @param threshold distance from target to consider "at target"
     * @param maxSpeed the fastest the robot will move, in m/s
     * @param alwaysMaxSpeed if true, the robot moves at maxSpeed for the full motion
     * @param minSpeed the slowest the robot will move in m/s, if alwaysMaxSpeed is false
     * @return true if distance to target is less than threshold
     */
    fun driveToPose(
        target: Pose2d,
        threshold: Distance = 0.inches,
        maxSpeed: Double = MAX_SPEED,
        alwaysMaxSpeed: Boolean = false,
        minSpeed: Double = 0.0,
    ): Boolean {
        Logger.recordOutput("swerve/driveToPoseTarget", target)
        val diff = target.translation - state.Pose.translation

        // The error is always positive, so the controller output is always negative.
        // Invert it to get a positive speed. diff gives the direction.
        val output = -posePIDController.calculate(diff.norm)
        val speed = if (alwaysMaxSpeed) maxSpeed else output.coerceIn(minSpeed, maxSpeed)
        val velocity = if (diff.norm > 1e-6) diff * (speed / diff.norm) else Translation2d()
        val limited = translationLimiter.calculate(velocity)

        setControl(
            driveToPoseRequest
                .withTargetDirection(target.rotation)
                .withVelocityX(limited.x)
                .withVelocityY(limited.y)
        )

        return diff.norm < threshold.meters
    }

    /**
     * Turns the robot around a point until it faces a heading. Call this one time each loop until
     * it returns true.
     *
     * @param targetHeading the field-relative heading to turn to
     * @param centerOfRotation the point to turn around, relative to the robot center
     * @param threshold heading error to consider "at target"
     * @param maxSpeed the fastest the robot center will move, in m/s
     * @param alwaysMaxSpeed if true, the robot turns at maxSpeed for the full motion
     * @return true if heading error is less than threshold
     */
    fun rotateAroundPoint(
        targetHeading: Rotation2d,
        centerOfRotation: Translation2d,
        threshold: Angle = 0.radians,
        maxSpeed: Double = MAX_SPEED,
        alwaysMaxSpeed: Boolean = false,
    ): Boolean {
        val error = (targetHeading - state.Pose.rotation).radians
        val maxRotation = maxSpeed / centerOfRotation.norm
        val rotation =
            if (alwaysMaxSpeed) sign(error) * maxRotation
            else
                headingPIDController
                    .calculate(state.Pose.rotation.radians, targetHeading.radians)
                    .coerceIn(-maxRotation, maxRotation)

        setControl(
            rotateAroundPointRequest
                .withRotationalRate(rotation)
                .withCenterOfRotation(centerOfRotation)
        )

        return abs(error).radians < threshold
    }

    private lateinit var simNotifier: Notifier
    private var lastSimTime = 0.0

    /* Keep track if we've ever applied the operator perspective before or not */
    private var hasAppliedOperatorPerspective = false

    init {
        if (Utils.isSimulation()) {
            startSimThread()
        }
    }

    fun periodic() {
        /*
         * Periodically try to apply the operator perspective.
         * If we haven't applied the operator perspective before, then we should apply it regardless of DS state.
         * This allows us to correct the perspective in case the robot code restarts mid-match.
         * Otherwise, only check and apply the operator perspective if the DS is disabled.
         * This ensures driving behavior doesn't change until an explicit disable event occurs during testing.
         */
        if (!hasAppliedOperatorPerspective || RobotState.isDisabled()) {
            MatchState.getAlliance().ifPresent {
                setOperatorForwardDirection(
                    if (it == Alliance.RED) kRedAlliancePerspectiveRotation
                    else kBlueAlliancePerspectiveRotation
                )
                hasAppliedOperatorPerspective = true
            }
        }

        Logger.recordOutput("swerve/state", currentState)
        Logger.recordOutput("swerve/lockedHeading", oldHeading)
    }

    private fun startSimThread() {
        lastSimTime = Utils.getCurrentTimeSeconds()

        /* Run simulation at a faster rate so PID gains behave more reasonably */
        simNotifier = Notifier {
            val currentTime = Utils.getCurrentTimeSeconds()
            val deltaTime = currentTime - lastSimTime
            lastSimTime = currentTime

            /* use the measured time delta, get battery voltage from WPILib */
            updateSimState(deltaTime, RobotController.getBatteryVoltage())
        }
        simNotifier.startPeriodic(kSimLoopPeriod)
    }

    private const val kSimLoopPeriod = 0.004 // 4 ms

    /* Blue alliance sees forward as 0 degrees (toward red alliance wall) */
    private val kBlueAlliancePerspectiveRotation: Rotation2d = Rotation2d.ZERO

    /* Red alliance sees forward as 180 degrees (toward blue alliance wall) */
    private val kRedAlliancePerspectiveRotation: Rotation2d = Rotation2d.k180deg

    /** Slew rate limiter on a 2D velocity vector. */
    private class TranslationSlewRateLimiter(private val rateLimit: Double) {
        private var prev = Translation2d()
        private var prevTime = MathSharedStore.getTimestamp()

        fun calculate(next: Translation2d): Translation2d {
            val currentTime = MathSharedStore.getTimestamp()
            val dt = currentTime - prevTime
            prevTime = currentTime
            prev = MathUtil.slewRateLimit(prev, next, dt, rateLimit)
            return prev
        }
    }
}
