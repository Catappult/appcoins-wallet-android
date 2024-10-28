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
        // KAPT must go last to avoid build warnings.
        // See: https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f
        apply("org.jetbrains.kotlin.kapt")
      }
      dependencies {
        "implementation"(libs["androidx-compose-hilt-navigation"])
        "implementation"(libs["hilt.android"])
        "kapt"(libs["hilt.compiler"])
        "implementation"(libs["hilt.interface.extensions"])
        "kapt"(libs["hilt-interface-extensions-processor"])
      }
    }
  }
}