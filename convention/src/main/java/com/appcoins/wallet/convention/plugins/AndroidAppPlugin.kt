package com.appcoins.wallet.convention.plugins

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.BuildConfigType
import com.appcoins.wallet.convention.extensions.buildConfigFields
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import kotlin.collections.set

class AndroidAppPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("kotlin-android")
        apply("kotlin-parcelize")
        apply("kotlin-kapt")
        apply<JacocoApplicationPlugin>()
      }

      extensions.configure<BaseAppModuleExtension> {
        configureAndroidAndKotlin(this)
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
            isShrinkResources = false
            enableUnitTestCoverage = true
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
            isMinifyEnabled = false
            isShrinkResources = false
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
          viewBinding {
            enable = true
          }
          composeOptions {
            kotlinCompilerExtensionVersion = "1.5.2"
          }
          compose = true
          aidl = true
        }

        flavorDimensions.add(Config.distributionFlavorDimension)
        productFlavors {
          create(Config.googlePlayDistribution) {
            dimension = Config.distributionFlavorDimension
          }
          create(Config.aptoidePlayDistribution) {
            dimension = Config.distributionFlavorDimension
          }
        }

      }
    }
  }
}


