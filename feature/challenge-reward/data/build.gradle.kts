plugins {
  id("appcoins.android.library.compose")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"
}

dependencies {
  implementation(project(":legacy:billing"))
  implementation(project(":core:analytics"))
  implementation(project(":core:network:microservices"))
  implementation(project(":feature:wallet-info:data"))
  implementation(libs.fyber.sdk)
  implementation(libs.rx.rxjava)
}
