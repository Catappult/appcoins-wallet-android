plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.flagr"
}

dependencies {
  implementation(project(":core:network:base"))
  implementation(project(":core:utils:properties"))
  implementation(libs.bundles.network)
}