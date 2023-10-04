plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.challengereward.data"

  @Suppress("UnstableApiUsage")
  buildFeatures {
    compose = true
  }

  @Suppress("UnstableApiUsage")
  composeOptions {
    kotlinCompilerExtensionVersion = "1.4.2"
  }
}

dependencies {
  implementation(project(":legacy:billing"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:analytics"))
  implementation(project(":feature:wallet-info:data"))

  // FYBER
  implementation("com.fyber:fairbid-sdk:3.42.0")

  // RX JAVA
  implementation("io.reactivex.rxjava2:rxjava:2.2.19")
}
