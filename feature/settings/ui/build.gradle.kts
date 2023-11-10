plugins {
  id("appcoins.android.library")
  id("appcoins.android.library.compose")
}

android { namespace = "com.appcoins.wallet.feature.settings.ui" }

dependencies {
  implementation(project(":feature:settings:data"))
  implementation(project(":feature:change-currency:data"))
  implementation(project(":feature:backup:ui"))
  implementation(project(":core:arch"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":ui:common"))
  implementation(project(":ui:widgets"))
  implementation(project(":core:legacy-base"))
  implementation(project(":core:analytics"))
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.rx)
  implementation(libs.androidx.fragment.ktx)
}
