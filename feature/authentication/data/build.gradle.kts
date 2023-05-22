plugins{
  id("appcoins.android.library")
}

android{
  namespace = "com.appcoins.wallet.feature.authentication.data"
}

dependencies{

  implementation(project(":feature:wallet-info:data"))
  implementation(project(":core:utils:android-common"))

  implementation(libs.bundles.rx)
}