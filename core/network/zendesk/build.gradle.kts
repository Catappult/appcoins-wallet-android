plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.zendesk"
}

dependencies {
  implementation(project(":core:network:base"))
}