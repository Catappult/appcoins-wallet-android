plugins{
  id("appcoins.jvm.library")
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))
  implementation(libs.rx.rxjava)
  implementation(libs.kotlin.stdlib)
  testImplementation(libs.bundles.testing)
}