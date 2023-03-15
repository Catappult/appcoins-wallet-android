plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.networkbase"
}

dependencies {
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk)
  implementation(project(":legacy:commons"))
  implementation(project(":core:shared-preferences"))
}