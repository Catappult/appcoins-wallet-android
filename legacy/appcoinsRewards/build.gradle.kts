plugins {
  id("appcoins.jvm.library")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":legacy:bdsbilling"))

  implementation(libs.bundles.network)
  testImplementation(libs.bundles.testing)
}