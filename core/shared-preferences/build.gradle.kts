plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.sharedpreferences"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(libs.androidx.security.crypto)
}