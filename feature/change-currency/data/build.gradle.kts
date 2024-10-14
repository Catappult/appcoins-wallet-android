plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.changecurrency.data"
}

dependencies {
  implementation(project(":core:arch"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:network:microservices"))
  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.bundles.androidx.room)
  implementation(libs.bundles.network)
  implementation(libs.bundles.result)
}