plugins {
  id("appcoins.android.library")
  id("appcoins.hilt")
}

android {
  defaultConfig {
//    versionCode 1
//    versionName "1.0"
  }
}
dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:commons"))

  implementation(libs.bundles.hilt.implementation)
  kapt(libs.bundles.hilt.kapt)

  implementation(libs.kotlin.stdlib)
  implementation(libs.bundles.androidx.room)
  kapt(libs.androidx.room.compiler)
  implementation(libs.bundles.network)
  implementation(libs.bundles.rx)
  testImplementation(libs.bundles.testing)
}