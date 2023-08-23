plugins{
  id("appcoins.android.library")
  id("kotlin-parcelize")
  id("appcoins.room")
}

android{
  namespace = "com.appcoins.wallet.feature.walletInfo.data"
}



dependencies{
  compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(project(":core:network:base"))
  implementation(project(":feature:promo-code:data"))
  implementation(project(":feature:change-currency:data"))
  implementation(project(":feature:support:data"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:network:backend"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:analytics"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":legacy:billing"))
  implementation(project(":legacy:tn"))
  implementation(project(":legacy:ethereumj-android"))
  implementation(project(":legacy:gamification"))
  implementation(project(":ui:common"))
  implementation(project(":core:walletServices"))
  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
  implementation(libs.bundles.result)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.bundles.rx)
  implementation(libs.web3j)
  implementation(libs.androidx.security.crypto)
  implementation(libs.google.gson)
}
