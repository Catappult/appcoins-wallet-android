plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.analytics"
}

dependencies {
  implementation(project(":core:network:base"))
  implementation(libs.bundles.network)
  implementation(libs.bundles.jackson)
}

