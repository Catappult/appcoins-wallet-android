plugins {
  id("appcoins.jvm.library")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))

  implementation(libs.bundles.network)
  implementation(libs.bundles.jackson)
  testImplementation(libs.bundles.testing)
}