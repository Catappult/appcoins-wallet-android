plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.bdsbilling"
}

dependencies {
  implementation(project(":core:network:microservices"))
  implementation(project(":core:network:bds"))
  implementation(project(":core:utils:jvm-common"))

  implementation(libs.bundles.network)
  implementation(libs.bundles.jackson)
  testImplementation(libs.bundles.testing)
}