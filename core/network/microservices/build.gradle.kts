plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.microservices"
}

dependencies {
  implementation(project(":core:network:base"))
  implementation(project(":core:utils:properties"))
  implementation(project(":core:utils:android-common"))
  implementation(libs.bundles.network)
  implementation(libs.bundles.rx)
  implementation(libs.bundles.jackson)
  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
}