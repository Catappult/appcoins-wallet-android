plugins{
  id("appcoins.android.library")
}

android{
  namespace = "com.appcoins.wallet.feature.promocode.data"
}

dependencies{
  implementation(project(":core:network:backend"))
  implementation(project(":core:analytics"))
  implementation(project(":core:utils:android-common"))
  implementation(libs.bundles.rx)
  implementation(libs.bundles.androidx.room)
  implementation(libs.bundles.network)
}