plugins {
  id("appcoins.android.app")
  id("appcoins.room")
  id("appcoins.hilt")
  id("kotlin-kapt")
}

android {
  defaultConfig {
    applicationId = "com.appcoins.wallet"
    versionCode = 258
    versionName = "2.9.1.0"

    signingConfigs {
      register("release") {
        storeFile = project.property("BDS_WALLET_STORE_FILE")?.let { file(it) }
        storePassword = project.property("BDS_WALLET_STORE_PASSWORD").toString()
        keyAlias = project.property("BDS_WALLET_KEY_ALIAS").toString()
        keyPassword = project.property("BDS_WALLET_KEY_PASSWORD").toString()
      }
    }

//    def inputFile = new File("$rootDir/appcoins-services.json")
//    def json = new JsonSlurper().parseText(inputFile.text)
//    buildConfigField("String", "DEFAULT_OEM_ADDRESS", "\"" + json.oems.default_address + "\"")
//    buildConfigField("String", "DEFAULT_STORE_ADDRESS", "\"" + json.stores.default_address + "\"")
//
//  buildTypes {
//    release {
//      signingConfig = signingConfigs.release
//      manifestPlaceholders.legacyPaymentHost = "${project.MANIFEST_LEGACY_PAYMENT_HOST}"
//      manifestPlaceholders.paymentHost = "${project.MANIFEST_PAYMENT_HOST}"
//    }
//    debug {
//      manifestPlaceholders.legacyPaymentHost = "${project.MANIFEST_LEGACY_PAYMENT_HOST_DEV}"
//      manifestPlaceholders.paymentHost = "${project.MANIFEST_PAYMENT_HOST_DEV}"
//    }
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":legacy:tn"))
  implementation(project(":legacy:airdrop"))
  implementation(project(":legacy:billing"))
  implementation(project(":legacy:commons"))
  implementation(project(":legacy:gamification"))
  implementation(project(":legacy:permissions"))
  implementation(project(":legacy:appcoinsRewards"))
  implementation(project(":legacy:skills"))
  implementation(project(":legacy:ethereumj-android"))

  implementation(libs.kotlin.coroutines)

  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.safeargs) // ??
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
  implementation(libs.intercom) {
    exclude(group = "com.google.android", module = "flexbox")
  }
  implementation(libs.paranoid)
  implementation(libs.flexbox)

  implementation(libs.bundles.analytics)

  implementation(libs.lottie)
  implementation(libs.shimmer)
  implementation(libs.glide)
  kapt(libs.glide.compiler)

  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)

  implementation(libs.appcoins.sdk)
  debugImplementation(libs.appcoins.sdk.debug)

  implementation(libs.web3j)
  implementation(libs.kethereum.erc681)

  implementation(libs.cpp)
  implementation(libs.commons.lang3)
  implementation(libs.android.support)
  implementation(libs.android.installreferrer)

  testImplementation(libs.bundles.testing)
  androidTestImplementation(libs.test.junit.ext)

  testImplementation(libs.test.junit.jupiter.api)
  testRuntimeOnly(libs.test.junit.jupiter.engine)
  testImplementation(libs.test.junit.jupiter.params)
  testImplementation(libs.test.junit.vintage.engine)
  testImplementation(libs.test.turbine)
  testImplementation(libs.kotlin.coroutines.test)
}