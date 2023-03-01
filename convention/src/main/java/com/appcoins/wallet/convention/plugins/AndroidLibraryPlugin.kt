@file:Suppress("UnstableApiUsage")

package com.appcoins.wallet.convention.plugins

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
import com.appcoins.wallet.convention.extensions.disableDebugBuildType
import com.appcoins.wallet.convention.extensions.get
import com.appcoins.wallet.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply<LibraryPlugin>()
        apply("com.android.library")
        apply("kotlin-android")
        apply("kotlin-kapt")
      }

      disableDebugBuildType()
      extensions.configure<LibraryExtension> {
        configureAndroidAndKotlin(this)
        buildToolsVersion = "30.0.3"
        defaultConfig.targetSdk = Config.android.targetSdk
      }

      dependencies {
        add("implementation", libs["kotlin.stdlib"])
      }
    }
  }
}