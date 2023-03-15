plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.eskills"
}

dependencies {
  implementation(project(":core:network:base"))
}