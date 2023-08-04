plugins{
  id("appcoins.android.library")
}

android{
  namespace = "com.appcoins.wallet.feature.backup.data"
}

dependencies{

  implementation(project(":core:utils:android-common"))
  implementation(project(":feature:wallet-info:data"))
  implementation(project(":core:network:backend"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":legacy:billing"))
  implementation(project(":core:walletServices"))
  implementation(project(":core:network:base"))
  implementation(libs.bundles.rx)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.google.material)
}