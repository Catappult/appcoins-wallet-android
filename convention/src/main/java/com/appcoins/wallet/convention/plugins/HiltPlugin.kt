package com.appcoins.wallet.convention.plugins

import com.appcoins.wallet.convention.extensions.get
import com.appcoins.wallet.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("dagger.hilt.android.plugin")
        apply("com.google.devtools.ksp")
      }
      dependencies {
        "implementation"(libs["androidx-compose-hilt-navigation"])
        "implementation"(libs["hilt.android"])
        "ksp"(libs["hilt.compiler"])
        "implementation"(libs["hilt.interface.extensions"])
        "ksp"(libs["hilt-interface-extensions-processor"])
      }
    }
  }
}