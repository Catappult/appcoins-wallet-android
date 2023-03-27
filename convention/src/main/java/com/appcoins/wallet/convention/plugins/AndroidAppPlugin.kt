package com.appcoins.wallet.convention.plugins

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.*
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
          buildConfigFields(project, rootDir)
          javaCompileOptions {
            annotationProcessorOptions {
              annotationProcessorOptions.arguments["room.schemaLocation"] =
                "${project.projectDir}/schemas"
            }
          }
        }

        signingConfigs {
          register("release") {
            storeFile = project.property("BDS_WALLET_STORE_FILE")?.let { file(it) }
            storePassword = project.property("BDS_WALLET_STORE_PASSWORD").toString()
            keyAlias = project.property("BDS_WALLET_KEY_ALIAS").toString()
            keyPassword = project.property("BDS_WALLET_KEY_PASSWORD").toString()
          }
        }

        buildTypes {
          debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = ".dev"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.DEBUG)
            manifestPlaceholders["legacyPaymentHost"] =
              project.property("MANIFEST_LEGACY_PAYMENT_HOST_DEV").toString()
            manifestPlaceholders["paymentHost"] =
              project.property("MANIFEST_PAYMENT_HOST_DEV").toString()
          }

          release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false //TODO this should be true, but its false since 2017
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.RELEASE)
            manifestPlaceholders["legacyPaymentHost"] =
              project.property("MANIFEST_LEGACY_PAYMENT_HOST").toString()
            manifestPlaceholders["paymentHost"] =
              project.property("MANIFEST_PAYMENT_HOST").toString()
          }

          register("staging") {
            initWith(getByName("release"))
            versionNameSuffix = ".staging"
          }
        }

        applicationVariants.all {
          val sep = "_"
          val buildType = buildType.name
          val versionName = versionName
          val versionCode = versionCode
          val fileName = "AppCoins_Wallet_v$versionName$sep$versionCode$sep$buildType.apk"
          outputs.all {
            (this as BaseVariantOutputImpl).outputFileName = fileName
          }
        }

        buildFeatures {
          buildConfig = true
          viewBinding = true
          composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("androidx.compose").get().toString()
          }  // "1.1.0"
          compose = true
        }
      }
    }
  }
}


