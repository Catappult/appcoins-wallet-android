plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.base"
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to "android-magnessdk-5.3.0.aar")))
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk)
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:shared-preferences"))
}
