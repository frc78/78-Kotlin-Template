package frc.robot

import com.ctre.phoenix6.SignalLogger
import frc.robot.auto.Auto
import frc.robot.generated.Telemetry
import frc.robot.subsystems.SwerveDrivetrain
import org.littletonrobotics.junction.LoggedRobot
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.networktables.LoggedNetworkChooser
import org.littletonrobotics.junction.networktables.NT4Publisher
import org.wpilib.driverstation.DriverStation
import org.wpilib.driverstation.internal.DriverStationBackend
import org.wpilib.system.DataLogManager
import org.wpilib.util.UsageReporting

object Robot : LoggedRobot() {
    private val autoChooser = LoggedNetworkChooser<Auto>("Auto")

    private var selectedAuto: Auto? = null

    init {
        UsageReporting.reportUsage("Language", "Kotlin")
        DriverStationBackend.silenceJoystickConnectionAlert(true)
        Logger.recordMetadata("GitBranch", GIT_BRANCH)
        Logger.recordMetadata("GitSHA", GIT_SHA)
        Logger.recordMetadata("GitDirty", DIRTY.toString())
        Logger.recordMetadata("BuildDate", BUILD_DATE)
        Logger.addDataReceiver(NT4Publisher())
        Logger.start()
        if (isReal()) {
            // DataLog will automatically log all NT changes. AKit logs to NT, DataLog logs NT to
            // file
            DataLogManager.start()
            SignalLogger.setPath("/U/ctre-logs")
            SignalLogger.start()
        }
        // Record both DS control and joystick data
        DriverStation.startDataLog(DataLogManager.getLog())
        SwerveDrivetrain.registerTelemetry(Telemetry::telemeterize)
    }

    override fun robotPeriodic() {
        SwerveDrivetrain.periodic()
    }

    override fun autonomousInit() {
        selectedAuto = autoChooser.get()
        selectedAuto?.init()
    }

    override fun autonomousPeriodic() {
        selectedAuto?.run()
    }

    override fun teleopPeriodic() {
        // Auto calls driveToPose() and rotateAroundPoint() directly,
        // so the drivetrain state machine runs only in teleop
        SwerveDrivetrain.stateTransitions()
        SwerveDrivetrain.stateActions()
    }

    override fun disabledInit() {
        // Re-enables brake mode on motors
        SwerveDrivetrain.idle()
    }
}
