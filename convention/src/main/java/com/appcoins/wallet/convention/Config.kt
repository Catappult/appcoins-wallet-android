package com.appcoins.wallet.convention

import org.gradle.api.JavaVersion

object Config {
  val android = AndroidConfig(
    minSdk = 21,
    targetSdk = 31,
    compileSdkVersion = 31,
  )
  val jvm = JvmConfig(
    javaVersion = JavaVersion.VERSION_1_8,
    kotlinJvm = "1.8",
    freeCompilerArgs = listOf(
      "-Xopt-in=kotlin.RequiresOptIn",
      // Enable experimental coroutines APIs, including Flow
      "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-Xopt-in=kotlinx.coroutines.FlowPreview",
      "-Xopt-in=kotlin.Experimental",
      // Enable experimental kotlinx serialization APIs
      "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
      "-Xsam-conversions=class"
    )
  )

  data class AndroidConfig(
    val minSdk: Int,
    val targetSdk: Int,
    val compileSdkVersion: Int,
  )

  data class JvmConfig(
    val javaVersion: JavaVersion,
    val kotlinJvm: String,
    val freeCompilerArgs: List<String>
  )
}