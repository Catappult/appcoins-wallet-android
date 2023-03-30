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
        kotlinCompilerExtensionVersion =
          "1.3.1"  // TODO libs.findVersion("androidx.compose").get().toString()
      }
      compose = true
    }
  }
}

dependencies {
  implementation(project(":ui:common"))

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.bundles.androidx.compose)

  implementation(libs.zxing.android)
  implementation(libs.glide)
  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)
  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.appcompact)
}