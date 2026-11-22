package frc.robot.lib

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.math.kinematics.SwerveDriveKinematics
import org.wpilib.math.linalg.Vector
import org.wpilib.math.numbers.N2
import org.wpilib.units.measure.Distance

inline val Double.signSquare
    get() = this * abs(this)

/**
 * Returns the factor to multiply robot-relative [velocities] by, so that the fastest module turns
 * at the fraction of [maxSpeed] that the driver asks for. The driver demand is the larger of the
 * translation and rotation fractions. No module saturates, and the direction of travel stays the
 * same. https://www.chiefdelphi.com/t/good-vs-bad-swerve/414439/32
 */
fun SwerveDriveKinematics.joystickScale(
    velocities: ChassisVelocities,
    maxSpeed: Double,
    maxRotSpeed: Double,
): Double {
    val demand =
        maxOf(hypot(velocities.vx, velocities.vy) / maxSpeed, abs(velocities.omega) / maxRotSpeed)
    val fastest = toSwerveModuleVelocities(velocities).maxOf { abs(it.velocity) }
    return if (fastest == 0.0) 1.0 else minOf(demand * maxSpeed / fastest, 1.0)
}

operator fun Vector<N2>.component1(): Double {
    return this[0]
}

operator fun Vector<N2>.component2(): Double {
    return this[1]
}

/**
 * Returns the pitch diameter of a #25 chain sprocket
 *
 * @param teeth The number of teeth on the sprocket
 */
fun pd25(teeth: Int): Distance {
    return (.25 / sin(PI / teeth)).inches
}
