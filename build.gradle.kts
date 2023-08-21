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
//TODO workaround waiting to be removed after compose.ui be fixed https://issuetracker.google.com/issues/293645018
allprojects {
  configurations.all { resolutionStrategy { force("androidx.emoji2:emoji2:1.3.0") } }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}