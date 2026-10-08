package com.appcoins.wallet.convention.plugins

import com.android.build.api.dsl.LibraryExtension
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
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
        apply("com.android.library")
        apply<HiltPlugin>()
        apply<JacocoLibraryPlugin>()
      }

      extensions.configure<LibraryExtension>("android") {
        configureAndroidAndKotlin(this)
        buildFeatures {
          // Required to generate BuildConfig constants in library modules.
          buildConfig = true
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

      dependencies {
        add("implementation", libs["kotlin.stdlib"])
      }
    }
  }
}