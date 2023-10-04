plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.feature.support.data"
}

dependencies{
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(libs.intercom) {
    exclude(group = "com.google.android", module = "flexbox")
  }
  implementation(project(":legacy:gamification"))
  implementation(project(":feature:promo-code:data"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:walletServices"))

  implementation(libs.bundles.result)
  implementation(libs.bundles.rx)
  implementation(libs.firebase.messaging)
  implementation(libs.google.play.services)


}