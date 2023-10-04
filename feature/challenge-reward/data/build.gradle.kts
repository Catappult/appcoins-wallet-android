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
    kotlinCompilerExtensionVersion = "1.4.4"
  }
}

dependencies {
  implementation(project(":legacy:billing"))
  implementation(project(":core:analytics"))
  implementation(project(":core:network:microservices"))
  implementation(project(":feature:wallet-info:data"))
  implementation(libs.fyber.sdk)
  implementation(libs.rx.rxjava)
}
