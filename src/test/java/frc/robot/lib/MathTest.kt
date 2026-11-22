package frc.robot.lib

import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.math.kinematics.SwerveDriveKinematics

class MathTest {
    private val kinematics =
        SwerveDriveKinematics(
            Translation2d(0.3, 0.3),
            Translation2d(0.3, -0.3),
            Translation2d(-0.3, 0.3),
            Translation2d(-0.3, -0.3),
        )
    private val maxSpeed = 4.0
    private val maxRot = 2 * PI

    private fun fastestAfterScale(speeds: ChassisVelocities): Double {
        val scaled = speeds * kinematics.joystickScale(speeds, maxSpeed, maxRot)
        return kinematics.toSwerveModuleVelocities(scaled).maxOf { abs(it.velocity) }
    }

    @Test
    fun fastestModuleMatchesDriverDemand() {
        assertEquals(4.0, fastestAfterScale(ChassisVelocities(4.0, 0.0, 0.0)), 1e-9)
        assertEquals(4.0, fastestAfterScale(ChassisVelocities(4.0, 0.0, maxRot)), 1e-9)
        assertEquals(2.0, fastestAfterScale(ChassisVelocities(0.0, 2.0, PI)), 1e-9)
    }

    @Test
    fun neverScalesUp() {
        assertEquals(
            1.0,
            kinematics.joystickScale(ChassisVelocities(0.0, 0.0, maxRot), maxSpeed, maxRot),
        )
        assertEquals(1.0, kinematics.joystickScale(ChassisVelocities(), maxSpeed, maxRot))
    }
}
