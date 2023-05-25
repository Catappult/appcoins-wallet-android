plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.ui.widgets"
  defaultConfig {
    buildFeatures {
      viewBinding = true
      composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
      }
      compose = true
    }
  }
}

dependencies {
  implementation(project(":ui:common"))
  implementation(project(":core:utils:android-common"))

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)

  implementation(libs.zxing.android)
  implementation(libs.glide)
  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)
  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.appcompact)
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.coil)
  implementation(libs.compose.lottie)
}
