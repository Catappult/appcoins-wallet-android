plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.bds"
}

dependencies {
  implementation(project(":core:network:base"))
}

