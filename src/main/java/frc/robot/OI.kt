package frc.robot

import frc.robot.lib.signSquare
import org.wpilib.driverstation.XboxController
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.linalg.VecBuilder
import org.wpilib.math.util.MathUtil

object OI {

    val driveController = XboxController(0)

    // -------------------------------------------------
    //      Driver Controller
    // -------------------------------------------------

    fun resetRobotYaw() = driveController.menuButton && driveController.viewButton

    fun xBrake() = driveController.xButton

    // Swerve drive

    /**
     * Left stick as a field-centric (x, y) vector. Squares the magnitude and keeps the direction.
     */
    fun translationInput(): Translation2d {
        val raw = VecBuilder.fill(-driveController.leftY, -driveController.leftX)
        val clamped = if (raw.norm() > 1.0) raw.unit() else raw
        return Translation2d(MathUtil.copyDirectionPow(clamped, 2.0))
    }

    fun rotInput() = -MathUtil.applyDeadband(driveController.rightX, 0.05).signSquare
}
