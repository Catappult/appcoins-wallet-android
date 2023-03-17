plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.bdsbilling"
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:network:bds"))

  implementation(libs.bundles.network)
  implementation(libs.bundles.jackson)
  testImplementation(libs.bundles.testing)
}