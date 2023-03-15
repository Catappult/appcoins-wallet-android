plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.backend"
}

dependencies {
  implementation(project(":core:network:base"))
}

