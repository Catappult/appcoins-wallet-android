plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.invoices.data"
}
dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompact)
}