package com.appcoins.wallet.convention.plugins

import com.appcoins.wallet.convention.extensions.get
import com.appcoins.wallet.convention.extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidFeaturePlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply {
        apply<AndroidLibraryPlugin>()
        apply<HiltPlugin>()
      }

      dependencies {
        add("implementation", libs["androidx.fragment.ktx"])
      }
    }
  }
}