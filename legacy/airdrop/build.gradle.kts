plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.airdrop"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(libs.bundles.network)
  implementation(libs.jetbrains.annotations)
  testImplementation(libs.bundles.testing)
  implementation(project(":core:network:airdrop"))
}