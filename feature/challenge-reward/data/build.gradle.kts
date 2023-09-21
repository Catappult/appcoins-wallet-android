plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"
}

dependencies {
  implementation(libs.fyber.sdk)
}
