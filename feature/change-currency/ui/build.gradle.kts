plugins {
  id("appcoins.android.library.compose")
}

android {
  namespace = "com.appcoins.wallet.feature.changecurrency.ui"
}

dependencies {
  implementation(project(":feature:change-currency:data"))
  implementation(project(":feature:backup:ui"))
  implementation(project(":core:arch"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":ui:common"))
  implementation(project(":ui:widgets"))
  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.bundles.androidx.room)
  implementation(libs.bundles.result)
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.coil)
}