plugins {
  id("appcoins.android.library.compose")
}

android {
  namespace = "com.appcoins.wallet.feature.vkpay"
}

dependencies {
  implementation(libs.bundles.vk)
}
