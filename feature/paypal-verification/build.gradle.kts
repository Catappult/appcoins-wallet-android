plugins { id("appcoins.android.library.compose") }

android { namespace = "com.appcoins.wallet.feature.paypalverification" }

dependencies {
  implementation(project(":core:arch"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":ui:common"))
  implementation(project(":ui:widgets"))
  implementation(libs.kotlin.coroutines)
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.coil)
}
