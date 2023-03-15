plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.sharedpreferences"
}

dependencies {
  implementation(libs.androidx.security.crypto)
}