plugins{
  id("appcoins.jvm.library")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(libs.bundles.rx)
  implementation(libs.kotlin.stdlib)
  testImplementation(libs.bundles.testing)
}
