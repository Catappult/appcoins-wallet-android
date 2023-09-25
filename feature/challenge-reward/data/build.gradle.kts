plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"
}

dependencies {
  implementation(project(":legacy:billing"))
  implementation(project(":core:network:microservices"))

  // FYBER
  implementation("com.fyber:fairbid-sdk:3.42.0")

  // RX JAVA
  implementation("io.reactivex.rxjava2:rxjava:2.2.19")
}
