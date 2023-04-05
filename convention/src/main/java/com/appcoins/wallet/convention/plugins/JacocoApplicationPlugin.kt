package com.appcoins.wallet.convention.plugins

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.appcoins.wallet.convention.extensions.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class JacocoApplicationPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("org.gradle.jacoco")
      }

      val extension = extensions.getByType<ApplicationAndroidComponentsExtension>()
      configureJacoco(extension)
    }
  }

}