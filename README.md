# 78's Kotlin Template

This template is a starting point for creating FRC projects in Kotlin. It has a few extra features compared to the template provided by the FRC plugin for IntelliJ

## Units

The WPILib Java Units Library is extremely verbose when used as is. Extension functions in Kotlin provide syntactic-sugar to make using units more enjoyable.

[Units.kt](src/main/java/lib/Units.kt) contains extension functions for commonly accessed units. 

```kotlin

val distance = 5.feet

val inches = distance.inches
```

## Command Delegate

Command factories in Java are presented as methods, usually with names that give the impression that calling said factory method will cause the robot to perform
some action at the time of invocation. For example, `goToScorePosition` is a conventional way of naming a command factory that, when scheduled, moves some mechanism to scoring position.
However, in a complex command, a student might accidentally call `goToScorePosition` inline, expecting it to move the robot into scoring position.

To make it clearer that these factories don't actually take some action, I find it better to present them as properties, with a getter.

```kotlin
val goToScorePosition
  get() = ...
```

To help remember the getter, `CommandDelegate` exists to essentially create a `command` keyword that students can use to remember when creating factories

```kotlin
val goToScorePosition by command {
  // create command here
}
```

## Spotless

Forcing code to be formatted is always nice. Spotless is set up to run for both java and kotlin source files. `ktfmt` is used for kotlin, and `google-java-format` for java. 

A github action to ensure the code has been formatted is also included


