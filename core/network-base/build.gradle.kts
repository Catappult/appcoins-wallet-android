plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("appcoins.hilt")
}

android {
  namespace = "com.example.network_base"
}

dependencies {
  implementation("androidx.core:core-ktx:1.7.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
}