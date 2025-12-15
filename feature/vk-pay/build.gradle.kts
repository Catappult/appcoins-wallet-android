plugins {
  id("appcoins.android.library.compose")
}

android {
  namespace = "com.appcoins.wallet.feature.vkpay"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(libs.bundles.vk)
}
