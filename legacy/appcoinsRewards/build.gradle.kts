plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.appcoinsrewards"
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))
  implementation(project(":legacy:bdsbilling"))
  implementation(project(":core:network:microservices"))

  implementation(libs.bundles.network)
  testImplementation(libs.bundles.testing)
}