package frc.robot.generated

import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveDriveState
import org.wpilib.telemetry.Telemetry

object Telemetry {
    /* Robot swerve drive state */
    private val driveStateTable = Telemetry.getTable("DriveState")

    /** Accept the swerve drive state and telemeterize it to SmartDashboard and SignalLogger. */
    fun telemeterize(state: SwerveDriveState) {
        /* Telemeterize the swerve drive state */
        driveStateTable.log("Pose", state.Pose)
        driveStateTable.log("Speeds", state.Velocity)
        driveStateTable.log(
            "FieldRelativeSpeeds",
            state.Velocity.toFieldRelative(state.Pose.rotation),
        )
        driveStateTable.log("ModuleStates", state.ModuleVelocities)
        driveStateTable.log("ModuleTargets", state.ModuleTargets)
        driveStateTable.log("ModulePositions", state.ModulePositions)
        driveStateTable.log("Timestamp", state.Timestamp)
        driveStateTable.log("OdometryFrequency", 1.0 / state.OdometryPeriod)
    }
}
