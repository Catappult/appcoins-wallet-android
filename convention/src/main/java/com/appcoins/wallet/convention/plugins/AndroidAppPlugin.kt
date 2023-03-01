package com.appcoins.wallet.convention.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("kotlin-android")
        apply("kotlin-android-extensions")
        apply("kotlin-kapt")
      }

      extensions.configure<BaseAppModuleExtension> {
        configureAndroidAndKotlin(this)
        buildToolsVersion = "30.0.3"
        ndkVersion = "21.3.6528147"
        defaultConfig {
          targetSdk = Config.android.targetSdk
          multiDexEnabled = true
          lint {
            abortOnError = false
          }
        }

        buildFeatures {
          buildConfig = true
          viewBinding = true
        }
      }
    }
  }
}


