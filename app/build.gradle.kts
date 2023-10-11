plugins {
  id("appcoins.android.app")
  id("appcoins.room")
  id("appcoins.hilt")
  id("appcoins.firebase")
  id("com.google.gms.google-services")
  id("androidx.navigation.safeargs.kotlin")
  id("de.mannodermaus.android-junit5")
}

android {
  defaultConfig {
    applicationId = "com.appcoins.wallet"
    versionCode = 278
    versionName = "3.3.1"
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(project(":legacy:tn"))
  implementation(project(":legacy:airdrop"))
  implementation(project(":legacy:billing"))
  implementation(project(":legacy:gamification"))
  implementation(project(":legacy:permissions"))
  implementation(project(":legacy:appcoinsRewards"))
  implementation(project(":legacy:skills"))
  implementation(project(":legacy:ethereumj-android"))
  implementation(project(":core:analytics"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:network:base"))
  implementation(project(":core:network:airdrop"))
  implementation(project(":core:network:analytics"))
  implementation(project(":core:network:backend"))
  implementation(project(":core:network:bds"))
  implementation(project(":core:network:eskills"))
  implementation(project(":core:network:microservices"))
  implementation(project(":core:network:zendesk"))
  implementation(project(":core:walletServices"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:utils:properties"))
  implementation(project(":core:arch"))
  implementation(project(":core:legacy-base"))
  implementation(project(":ui:common"))
  implementation(project(":ui:widgets"))
  implementation(project(":feature:change-currency:data"))
  implementation(project(":feature:change-currency:ui"))
  implementation(project(":feature:wallet-info:data"))
  implementation(project(":feature:backup:data"))
  implementation(project(":feature:support:data"))
  implementation(project(":feature:backup:ui"))
  implementation(project(":feature:promo-code:data"))
  implementation(project(":home"))

  implementation(libs.kotlin.coroutines)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.bundles.result)

  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.fragment)

  implementation(libs.androidx.appcompact)
  implementation(libs.androidx.vectordrawable)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.cardview)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.palette)
  implementation(libs.androidx.multidex)
  implementation(libs.androidx.viewpager)
  implementation(libs.androidx.browser)
  implementation(libs.androidx.biometric)
  implementation(libs.androidx.security.crypto)

  implementation(libs.bundles.androidx.lifecycle)
  implementation(libs.bundles.androidx.work)
  implementation(libs.bundles.androidx.compose)

  implementation(libs.bundles.network)

  implementation(libs.bundles.rx)
  implementation(libs.rx.rxlifecyle)
  implementation(libs.rx.rxlifecyle.components)

  implementation(libs.google.material)
  implementation(libs.google.play.services)
  implementation(libs.google.zxing)
  implementation(libs.zxing.android)

  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
  implementation(libs.firebase.messaging)
  implementation(libs.intercom) { exclude(group = "com.google.android", module = "flexbox") }
  implementation(libs.paranoid)
  implementation(libs.flexbox)

  implementation(libs.bundles.analytics)

  implementation(libs.lottie)
  implementation(libs.shimmer)
  implementation(libs.glide)
  kapt(libs.glide.compiler)
  implementation(libs.bundles.coil)

  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)

  implementation(libs.appcoins.sdk)
  debugImplementation(libs.appcoins.sdk.debug)

  implementation(libs.web3j)
  implementation(libs.kethereum.erc681)

  implementation(libs.cpp)
  implementation(libs.commons.lang3)
  implementation(libs.android.support.annotations)
  implementation(libs.android.installreferrer)

  implementation(libs.bundles.paging)

  testImplementation(libs.bundles.testing)
  androidTestImplementation(libs.test.junit.ext)

  testImplementation(libs.test.junit.jupiter.api)
  testRuntimeOnly(libs.test.junit.jupiter.engine)
  testImplementation(libs.test.junit.jupiter.params)
  testImplementation(libs.test.junit.vintage.engine)
  testImplementation(libs.test.turbine)
  testImplementation(libs.kotlin.coroutines.test)
}
