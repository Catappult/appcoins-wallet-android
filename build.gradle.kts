// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath(libs.gradlePlugin.android)
    classpath(libs.gradlePlugin.kotlin)
    classpath(libs.gradlePlugin.ksp)
    classpath(libs.gradlePlugin.hilt)
    classpath(libs.gradlePlugin.junit5)
    classpath(libs.google.services)
    classpath(libs.androidx.navigation.safeargs)
//    classpath(libs.gradlePlugin.bintray)
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}