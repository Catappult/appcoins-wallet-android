plugins {
  id("appcoins.android.library")
  id("appcoins.room")
}
android {
  namespace = "com.appcoins.wallet.gamification"
}
dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:network:backend"))

  implementation(libs.bundles.network)
  implementation(libs.bundles.rx)
  testImplementation(libs.bundles.testing)
}