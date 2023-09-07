plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.personalinfo.data"
}
dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompact)
}