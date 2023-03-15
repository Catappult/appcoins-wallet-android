plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.example.network_base"
}

dependencies {
  implementation(libs.bundles.network)
  implementation(project(":legacy:commons"))
}