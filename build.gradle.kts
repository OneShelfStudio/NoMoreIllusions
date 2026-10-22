// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.github.triplet.play") version "3.12.0" apply false
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

allprojects {
    layout.buildDirectory.set(
        file("${System.getProperty("user.home")}/.gradle/nomoreillusions-build/${project.name}")
    )
}
