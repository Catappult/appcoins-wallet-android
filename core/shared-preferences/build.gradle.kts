plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.sharedpreferences"
}

dependencies {
  implementation(libs.androidx.security.crypto)
}