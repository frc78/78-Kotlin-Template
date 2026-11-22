package frc.robot.lib

import org.wpilib.math.util.MathUtil
import org.wpilib.units.AngularAccelerationUnit
import org.wpilib.units.Measure
import org.wpilib.units.MomentOfInertiaUnit
import org.wpilib.units.TimeUnit
import org.wpilib.units.Units.Amps
import org.wpilib.units.Units.Centimeters
import org.wpilib.units.Units.Degrees
import org.wpilib.units.Units.DegreesPerSecond
import org.wpilib.units.Units.Feet
import org.wpilib.units.Units.FeetPerSecond
import org.wpilib.units.Units.Hertz
import org.wpilib.units.Units.Inches
import org.wpilib.units.Units.InchesPerSecond
import org.wpilib.units.Units.InchesPerSecondPerSecond
import org.wpilib.units.Units.KilogramSquareMeters
import org.wpilib.units.Units.Kilograms
import org.wpilib.units.Units.Meters
import org.wpilib.units.Units.MetersPerSecond
import org.wpilib.units.Units.MetersPerSecondPerSecond
import org.wpilib.units.Units.Milliseconds
import org.wpilib.units.Units.Pounds
import org.wpilib.units.Units.RPM
import org.wpilib.units.Units.Radians
import org.wpilib.units.Units.RadiansPerSecond
import org.wpilib.units.Units.RadiansPerSecondPerSecond
import org.wpilib.units.Units.Rotations
import org.wpilib.units.Units.RotationsPerSecond
import org.wpilib.units.Units.RotationsPerSecondPerSecond
import org.wpilib.units.Units.Second
import org.wpilib.units.Units.Seconds
import org.wpilib.units.Units.Volts
import org.wpilib.units.VoltageUnit
import org.wpilib.units.measure.Angle
import org.wpilib.units.measure.AngularAcceleration
import org.wpilib.units.measure.AngularVelocity
import org.wpilib.units.measure.Current
import org.wpilib.units.measure.Distance
import org.wpilib.units.measure.LinearAcceleration
import org.wpilib.units.measure.LinearVelocity
import org.wpilib.units.measure.Mass
import org.wpilib.units.measure.MomentOfInertia
import org.wpilib.units.measure.Time
import org.wpilib.units.measure.Velocity
import org.wpilib.units.measure.Voltage

/* Extension properties are ways of adding functionality to classes
 * we didn't create, without having to create a subclass.*/

// These extension properties add units to numbers to make more readable code
// You can use them like so:
// val length = 5.inches
// val angle = 10.degrees
val Number.inches: Distance
    get() = Inches.of(this.toDouble())
val Number.feet: Distance
    get() = Feet.of(this.toDouble())
val Number.meters: Distance
    get() = Meters.of(this.toDouble())
val Number.centimeters: Distance
    get() = Centimeters.of(this.toDouble())
val Number.milliseconds: Time
    get() = Milliseconds.of(this.toDouble())
val Number.seconds: Time
    get() = Seconds.of(this.toDouble())
val Number.metersPerSecond: LinearVelocity
    get() = MetersPerSecond.of(this.toDouble())
val Number.inchesPerSecond: LinearVelocity
    get() = InchesPerSecond.of(this.toDouble())
val Number.inchesPerSecondPerSecond: LinearAcceleration
    get() = InchesPerSecondPerSecond.of(this.toDouble())
val Number.feetPerSecond: LinearVelocity
    get() = FeetPerSecond.of(this.toDouble())
val Number.volts: Voltage
    get() = Volts.of(this.toDouble())
val Number.amps: Current
    get() = Amps.of(this.toDouble())
val Number.metersPerSecondPerSecond: LinearAcceleration
    get() = MetersPerSecondPerSecond.of(this.toDouble())
val Number.radians: Angle
    get() = Radians.of(this.toDouble())
val Number.radiansPerSecond: AngularVelocity
    get() = RadiansPerSecond.of(this.toDouble())
val Number.radiansPerSecondPerSecond: AngularAcceleration
    get() = RadiansPerSecondPerSecond.of(this.toDouble())
val Number.rpm: AngularVelocity
    get() = RPM.of(this.toDouble())
val Number.degrees: Angle
    get() = Degrees.of(this.toDouble())
val Number.rotationsPerSecond: AngularVelocity
    get() = RotationsPerSecond.of(this.toDouble())
val Number.rotationsPerSecondPerSecond: AngularAcceleration
    get() = RotationsPerSecondPerSecond.of(this.toDouble())
val Number.rotationsPerSecondCubed: Velocity<AngularAccelerationUnit>
    get() = RotationsPerSecondPerSecond.of(this.toDouble()).per(Second)
val Number.voltsPerSecond: Velocity<VoltageUnit>
    get() = Volts.of(this.toDouble()).per(Second)
val Number.rotations: Angle
    get() = Rotations.of(this.toDouble())
val Number.pounds: Mass
    get() = Pounds.of(this.toDouble())
val Number.kilogramSquareMeters: MomentOfInertia
    get() = KilogramSquareMeters.of(this.toDouble())
val Number.poundSquareInches: MomentOfInertia
    get() = PoundSquareInches.of(this.toDouble())
val Number.hz: org.wpilib.units.measure.Frequency
    get() = Hertz.of(this.toDouble())

val PoundSquareInches: MomentOfInertiaUnit =
    MomentOfInertiaUnit.combine(Pounds.mult(InchesPerSecond).mult(Inches), RadiansPerSecond)

// These extension properties allow converting from a unit to a raw value
// You can use them like so:
// val meters = Meters.of(10)
// val tenMetersInInches = meters.inches
// val rotations = Rotations.of(5)
// val fiveRotationsInDegrees = rotations.degrees
val Distance.inches
    get() = this.`in`(Inches)
val Distance.meters
    get() = this.`in`(Meters)
val Distance.centimeters
    get() = this.`in`(Centimeters)
val LinearVelocity.metersPerSecond
    get() = this.`in`(MetersPerSecond)
val LinearAcceleration.metersPerSecondPerSecond
    get() = this.`in`(MetersPerSecondPerSecond)
val AngularVelocity.radiansPerSecond
    get() = this.`in`(RadiansPerSecond)
val AngularVelocity.rotationsPerSecond
    get() = this.`in`(RotationsPerSecond)
val AngularVelocity.degreesPerSecond
    get() = this.`in`(DegreesPerSecond)
val AngularVelocity.rpm
    get() = this.`in`(RPM)
val AngularAcceleration.radiansPerSecondPerSecond
    get() = this.`in`(RadiansPerSecondPerSecond)
val Voltage.volts
    get() = this.`in`(Volts)
val Angle.radians
    get() = this.`in`(Radians)
val Angle.degrees
    get() = this.`in`(Degrees)
val Angle.rotations
    get() = this.`in`(Rotations)
val Current.amps
    get() = this.`in`(Amps)
val Mass.kilograms
    get() = this.`in`(Kilograms)
val MomentOfInertia.kilogramSquareMeters
    get() = this.`in`(KilogramSquareMeters)
val Measure<TimeUnit>.seconds
    get() = this.`in`(Seconds)

fun Angle.toDistance(radius: Distance): Distance = radius * this.radians

fun Distance.toAngle(radius: Distance): Angle = Radians.of((this / radius).magnitude())

fun LinearVelocity.toAngularVelocity(radius: Distance): AngularVelocity =
    RadiansPerSecond.of(this.metersPerSecond / radius.meters)

fun LinearAcceleration.toAngularAcceleration(radius: Distance): AngularAcceleration =
    RadiansPerSecondPerSecond.of(this.metersPerSecondPerSecond / radius.meters)

fun AngularVelocity.toLinearVelocity(radius: Distance): LinearVelocity =
    MetersPerSecond.of(radiansPerSecond * radius.meters)

fun <U : Measure<*>> U.interpolate(endValue: U, t: Double): U {
    return unit().ofBaseUnits(MathUtil.lerp(baseUnitMagnitude(), endValue.baseUnitMagnitude(), t))
        as U
}
