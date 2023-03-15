plugins {
  id("appcoins.android.library")
}
android {
  namespace = "com.appcoins.wallet.core.utils.common"
}
dependencies {
  implementation(libs.web3j)
  implementation(libs.androidx.navigation.ui)
}