package com.appcoins.wallet.convention.extensions

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: LibraryExtension,
) {
  pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

  commonExtension.apply {
    buildFeatures {
      compose = true
    }
  }
}