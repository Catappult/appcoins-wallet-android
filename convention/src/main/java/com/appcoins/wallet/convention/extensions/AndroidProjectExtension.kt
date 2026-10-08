package com.appcoins.wallet.convention.extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.appcoins.wallet.convention.Config
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

private const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
private const val META_INF_NOTICE = "META-INF/NOTICE"
private const val META_INF_LICENCE = "META-INF/LICENSE"
private const val META_INF_LICENCE_MD = "META-INF/LICENSE.md"
private const val META_INF_NOTICE_MD = "META-INF/LICENSE-notice.md"
private const val CERTIFICATES = "org/bouncycastle/x509/CertPathReviewerMessages*.properties"
private const val META_INF_MIT = "META-INF/LICENSE-MIT"
private const val DESUGAR_CONF = "coreLibraryDesugaring"
private const val DESUGAR_LIB = "android.desugar"

/**
 * Configures baseline Android and Kotlin settings for an Android application module.
 *
 * @receiver the [Project] being configured.
 * @param extension the [ApplicationExtension] instance for the app module.
 */
internal fun Project.configureAndroidAndKotlin(extension: ApplicationExtension) {
  extension.compileSdk = Config.android.compileSdkVersion
  extension.defaultConfig {
    minSdk = Config.android.minSdk
    testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
  }
  extension.compileOptions {
    sourceCompatibility = Config.jvm.javaVersion
    targetCompatibility = Config.jvm.javaVersion
    isCoreLibraryDesugaringEnabled = true
  }
  extension.packaging {
    resources {
      excludes += listOf(
        META_INF_NOTICE,
        META_INF_LICENCE,
        META_INF_LICENCE_MD,
        META_INF_NOTICE_MD
      )
      pickFirsts += listOf(
        CERTIFICATES,
        META_INF_MIT
      )
    }
  }
  configureKotlinAndDesugar()
}

/**
 * Configures baseline Android and Kotlin settings for an Android library module.
 *
 * @receiver the [Project] being configured.
 * @param extension the [LibraryExtension] instance for the library module.
 */
internal fun Project.configureAndroidAndKotlin(extension: LibraryExtension) {
  extension.compileSdk = Config.android.compileSdkVersion
  extension.defaultConfig {
    minSdk = Config.android.minSdk
    testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
  }
  extension.compileOptions {
    sourceCompatibility = Config.jvm.javaVersion
    targetCompatibility = Config.jvm.javaVersion
    isCoreLibraryDesugaringEnabled = true
  }
  extension.packaging {
    resources {
      excludes += listOf(
        META_INF_NOTICE,
        META_INF_LICENCE,
        META_INF_LICENCE_MD,
        META_INF_NOTICE_MD
      )
      pickFirsts += listOf(
        CERTIFICATES,
        META_INF_MIT
      )
    }
  }
  configureKotlinAndDesugar()
}

private fun Project.configureKotlinAndDesugar() {
  tasks
    .withType<KotlinJvmCompile>()
    .configureEach {
      compilerOptions {
        jvmTarget.set(Config.jvm.kotlinJvm)
        freeCompilerArgs.addAll(Config.jvm.freeCompilerArgs)
      }
    }

  dependencies
    .apply {
      add(DESUGAR_CONF, libs[DESUGAR_LIB])
    }
}