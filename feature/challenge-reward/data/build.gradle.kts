plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"
}

dependencies {
  implementation(project(":legacy:billing"))
  implementation(project(":core:network:microservices"))
  implementation(libs.fyber.sdk)
  implementation(libs.rx.rxjava)
}
