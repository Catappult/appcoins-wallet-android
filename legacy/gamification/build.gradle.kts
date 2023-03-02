plugins {
  id("appcoins.android.library")
  id("appcoins.room")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))

  implementation(libs.bundles.network)
  implementation(libs.bundles.rx)
  testImplementation(libs.bundles.testing)
}