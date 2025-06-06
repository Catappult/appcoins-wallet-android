plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.billing"

  buildFeatures {
    aidl = true
  }
}

dependencies {
  api(project(":legacy:bdsbilling"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:utils:properties"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:network:bds"))
  implementation(project(":core:walletServices"))
  implementation(project(":core:network:base"))
  implementation(project(":core:analytics"))
  implementation(project(":core:shared-preferences"))

  implementation(libs.bundles.rx)
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk.communication)
  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
  implementation(libs.bundles.jackson)
  implementation(libs.spongycastle.core)
  testImplementation(libs.bundles.testing)
  testImplementation(project(":legacy:ethereumj-android"))
}
