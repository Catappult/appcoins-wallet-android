plugins {
  id("appcoins.android.library")
}
android {
  namespace = "com.appcoins.wallet.core.utils.android_common"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}
dependencies {
  implementation(libs.web3j)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.bundles.rx)
  implementation(libs.network.retrofit)
}