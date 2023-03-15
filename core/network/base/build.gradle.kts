plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.base"
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to "*.aar")))
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk)
  implementation(project(":legacy:commons"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:common"))
}
