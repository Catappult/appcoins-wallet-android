package com.appcoins.wallet.convention.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.appcoins.wallet.convention.extensions.libs
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FirebasePlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.google.gms.google-services")
        apply("com.google.firebase.firebase-perf")
        apply("com.google.firebase.crashlytics")
      }

      dependencies {
        val bom = libs.findLibrary("firebase-bom").get()
        add("implementation", platform(bom))
        "implementation"(libs.findLibrary("firebase.analytics").get())
        "implementation"(libs.findLibrary("firebase.performance").get())
        "implementation"(libs.findLibrary("firebase.crashlytics").get())
        "implementation"(libs.findLibrary("firebase.messaging").get())
      }

      extensions.configure<ApplicationExtension> {
        buildTypes.configureEach {
          configure<CrashlyticsExtension> { mappingFileUploadEnabled = false }
        }
      }
    }
  }
}
