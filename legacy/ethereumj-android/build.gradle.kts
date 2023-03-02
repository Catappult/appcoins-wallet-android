plugins {
  id("appcoins.android.library")
}

dependencies {
  implementation(libs.google.material)
  implementation(libs.androidx.appcompact)
  implementation(libs.androidx.core.ktx)
  implementation(libs.bundles.jackson)
  implementation(libs.commons.lang3)
  implementation(libs.spongycastle.prov)
  testImplementation(libs.bundles.testing)
  androidTestImplementation(libs.test.junit.ext)
  androidTestImplementation(libs.test.espresso)
}
