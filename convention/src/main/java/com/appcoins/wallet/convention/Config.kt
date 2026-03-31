package com.appcoins.wallet.convention

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object Config {
  val android = AndroidConfig(
    minSdk = 24,
    targetSdk = 35,
    compileSdkVersion = 35,
    ndkVersion = "28.2.13676358"
  )
  val jvm = JvmConfig(
    javaVersion = JavaVersion.VERSION_17,
    kotlinJvm = JvmTarget.JVM_17,
    freeCompilerArgs = listOf(
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlin.Experimental",
      "-Xsam-conversions=class"
    )
  )

  data class AndroidConfig(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdkVersion: Int,
    val ndkVersion: String,
  )

  data class JvmConfig(
    val javaVersion: JavaVersion,
    val kotlinJvm: JvmTarget,
    val freeCompilerArgs: List<String>
  )

  val distributionFlavorDimension = "distribution"
  val googlePlayDistribution = "gp"
  val aptoidePlayDistribution = "aptoide"
}