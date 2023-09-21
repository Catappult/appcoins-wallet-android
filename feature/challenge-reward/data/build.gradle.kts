plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"
}

dependencies {
  // FYBER
  implementation("com.fyber:fairbid-sdk:3.42.0")
}
