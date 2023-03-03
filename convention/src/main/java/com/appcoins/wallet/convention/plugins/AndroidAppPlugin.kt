package com.appcoins.wallet.convention.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.BuildConfigType
import com.appcoins.wallet.convention.extensions.buildConfigFields
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
import com.appcoins.wallet.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.File

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
        buildToolsVersion = Config.android.buildToolsVersion
        ndkVersion = Config.android.ndkVersion
        defaultConfig {
          targetSdk = Config.android.targetSdk
          multiDexEnabled = true
          lint {
            abortOnError = false
          }
          buildConfigFields(project)
        }

        buildTypes {
          debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = ".dev"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.DEBUG)
          }

          release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.RELEASE)
          }

//          getByName("staging") {
//            initWith(getByName("release"))
//            versionNameSuffix = ".staging"
//          }

          applicationVariants.all() { variant ->
            val sep = "_"
            val buildType = variant.buildType.name
            val versionName = variant.versionName
            val versionCode = variant.versionCode
            val fileName = "AppCoins_Wallet_v$versionName$sep$versionCode$sep$buildType.apk"
            variant.outputs.all { output ->
              val newFile = File(output.outputFile.parent, fileName)
              output.outputFile.renameTo(newFile)
            }
          }
        }

        buildFeatures {
          buildConfig = true
          viewBinding = true
          composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("androidx.compose").get().toString()
          }
        }
      }
    }
  }
}


