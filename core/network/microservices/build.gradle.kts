plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.microservices"
}

dependencies {
  implementation(project(":core:network:base"))
}