plugins {
  id("appcoins.android.library.compose")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.ui.widgets"
  defaultConfig {
    buildFeatures {
      viewBinding = true
    }
  }

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(project(":ui:common"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:analytics"))

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)

  implementation(libs.zxing.android)
  implementation(libs.glide)
  implementation(libs.epoxy)
  ksp(libs.epoxy.processor)
  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.appcompact)
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.coil)
  implementation(libs.compose.lottie)
}
