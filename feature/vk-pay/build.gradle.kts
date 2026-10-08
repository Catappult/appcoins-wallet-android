plugins {
  id("appcoins.android.library.compose")
}

android {
  namespace = "com.appcoins.wallet.feature.vkpay"

  buildFeatures {
    buildConfig = true
  }

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(libs.bundles.vk)
}
