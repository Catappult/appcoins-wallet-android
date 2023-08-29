plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.base"
}

dependencies {
  compileOnly(fileTree(mapOf("dir" to "libs", "include" to "*.aar")))
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk)
  implementation(libs.bundles.result)
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:arch"))
  implementation(project(":core:walletServices"))
}
