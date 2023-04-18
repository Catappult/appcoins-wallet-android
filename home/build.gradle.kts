plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.home"
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
  implementation(project(":ui:arch"))
  implementation(project(":ui:widgets"))

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
