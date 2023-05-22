plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.legacy_base"
}

dependencies{
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(project(":core:analytics"))
  implementation(project(":core:utils:android-common"))
  implementation(libs.androidx.appcompact)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.bundles.result)
  implementation(libs.network.retrofit)

}