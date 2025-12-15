plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.bds"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(project(":core:network:base"))
  implementation(project(":core:utils:properties"))
  implementation(libs.bundles.network)
}

