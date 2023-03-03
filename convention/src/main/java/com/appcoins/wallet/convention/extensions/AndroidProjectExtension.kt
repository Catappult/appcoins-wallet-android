package com.appcoins.wallet.convention.extensions

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.appcoins.wallet.convention.Config
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

internal fun Project.disableDebugBuildType() {
  extensions.configure(AndroidComponentsExtension::class.java) {
    beforeVariants(selector().withBuildType("debug")) { builder ->
      builder.enable = false
    }
  }
}

internal fun Project.configureAndroidAndKotlin(extension: CommonExtension<*, *, *, *>) {
  with(extension) {
    compileSdk = Config.android.compileSdkVersion
    defaultConfig {
      minSdk = Config.android.minSdk
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
      sourceCompatibility = Config.jvm.javaVersion
      targetCompatibility = Config.jvm.javaVersion
      isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
      jvmTarget = Config.jvm.kotlinJvm
      freeCompilerArgs = freeCompilerArgs + Config.jvm.freeCompilerArgs
    }

    packagingOptions {
//      resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
      resources.excludes += "META-INF/NOTICE"
      resources.excludes += "META-INF/LICENSE"
      // Pick the first version of certain resources
      resources.pickFirsts += "org/bouncycastle/x509/CertPathReviewerMessages_de.properties"
      resources.pickFirsts += "org/bouncycastle/x509/CertPathReviewerMessages.properties"
    }
  }

  dependencies.apply {
    add("coreLibraryDesugaring", libs["android.desugar"])
  }
}


private fun CommonExtension<*, *, *, *>.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
  (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}