plugins {
  id("appcoins.android.library")
}

android {
  defaultConfig {
//    versionCode 1
//    versionName "1.0"
  }
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
