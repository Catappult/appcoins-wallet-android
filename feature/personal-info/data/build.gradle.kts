plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.personalinfo.data"
}
dependencies {
  implementation(project(":core:network:base"))
  implementation(project(":core:network:backend"))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompact)
  implementation(libs.bundles.jackson)
  implementation(libs.bundles.network)
}