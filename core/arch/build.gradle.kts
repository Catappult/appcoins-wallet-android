plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.arch"
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(project(":core:utils:android-common"))
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.appcompact)
  implementation(libs.bundles.rx)
  implementation(libs.rx.rxlifecyle)
  implementation(libs.bundles.androidx.lifecycle)
  implementation(libs.bundles.result)
}
