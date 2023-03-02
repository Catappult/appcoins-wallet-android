import groovy.json.JsonSlurper

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

//    def inputFile = new File("$rootDir/appcoins-services.json")
//    def json = new JsonSlurper().parseText(inputFile.text)
//    buildConfigField("String", "DEFAULT_OEM_ADDRESS", "\"" + json.oems.default_address + "\"")
//    buildConfigField("String", "DEFAULT_STORE_ADDRESS", "\"" + json.stores.default_address + "\"")
//
//    buildConfigField("int", "DB_VERSION", "5")
//    buildConfigField("int", "BILLING_SUPPORTED_VERSION", project.BILLING_SUPPORTED_VERSION)
//    buildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_SYMBOL", project.ROPSTEN_DEFAULT_TOKEN_SYMBOL)
//    buildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_ADDRESS", project.ROPSTEN_DEFAULT_TOKEN_ADDRESS)
//    buildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_NAME", project.MAIN_NETWORK_DEFAULT_TOKEN_NAME)
//    buildConfigField("int", "ROPSTEN_DEFAULT_TOKEN_DECIMALS", project.ROPSTEN_DEFAULT_TOKEN_DECIMALS)
//    buildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL", project.MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL)
//    buildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS", project.MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS)
//    buildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_NAME", project.ROPSTEN_DEFAULT_TOKEN_NAME)
//    buildConfigField("int", "MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS", project.MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS)
//    buildConfigField("String", "PAYMENT_GAS_LIMIT", project.PAYMENT_GAS_LIMIT)
//    buildConfigField("String", "FLURRY_APK_KEY", project.FLURRY_APK_KEY)
//    buildConfigField("String", "PAYMENT_HOST_ROPSTEN_NETWORK", project.PAYMENT_HOST_DEV)
//    buildConfigField("String", "PAYMENT_HOST", project.PAYMENT_HOST)
//    buildConfigField("String", "TRANSACTION_DETAILS_HOST", project.TRANSACTION_DETAILS_HOST)
//    buildConfigField("String", "TRANSACTION_DETAILS_HOST_ROPSTEN", project.TRANSACTION_DETAILS_HOST_ROPSTEN)
//    buildConfigField("String", "RAKAM_BASE_HOST", project.RAKAM_BASE_HOST)
//    buildConfigField("String", "RAKAM_API_KEY", project.RAKAM_API_KEY)
//    buildConfigField("String", "APTOIDE_WEB_SERVICES_AB_TEST_HOST", project.APTOIDE_WEB_SERVICES_AB_TEST_HOST)
//    buildConfigField("String", "INFURA_API_KEY_MAIN", project.INFURA_API_KEY_MAIN)
//    buildConfigField("String", "INFURA_API_KEY_ROPSTEN", project.INFURA_API_KEY_ROPSTEN)
//    buildConfigField("String", "INFURA_API_KEY_RINKEBY", project.INFURA_API_KEY_RINKEBY)
//    buildConfigField("String", "AMPLITUDE_API_KEY", project.AMPLITUDE_API_KEY)
//    buildConfigField("String", "FEEDBACK_ZENDESK_API_KEY", project.FEEDBACK_ZENDESK_API_KEY)
//    buildConfigField("String", "FEEDBACK_ZENDESK_BASE_HOST", project.FEEDBACK_ZENDESK_BASE_HOST)
//    buildConfigField("String", "APTOIDE_TOP_APPS_URL", project.APTOIDE_TOP_APPS_URL)
//    buildConfigField("String", "VIP_PROGRAM_BADGE_URL", project.VIP_PROGRAM_BADGE_URL)
//    buildConfigField("String", "APPSFLYER_KEY", project.APPSFLYER_KEY)
//    buildConfigField("String", "TERMS_CONDITIONS_URL", project.TERMS_CONDITIONS_URL)
//    buildConfigField("String", "PRIVACY_POLICY_URL", project.PRIVACY_POLICY_URL)
//  }
//
//  signingConfigs {
//    release {
//      storeFile = file(project.BDS_WALLET_STORE_FILE)
//      storePassword = project.BDS_WALLET_STORE_PASSWORD
//      keyAlias = project.BDS_WALLET_KEY_ALIAS
//      keyPassword = project.BDS_WALLET_KEY_PASSWORD
//    }
//  }
//
//  buildTypes {
//    release {
//      minifyEnabled = false
//      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
//      signingConfig = signingConfigs.release
//      buildConfigField("int", "LEADING_ZEROS_ON_PROOF_OF_ATTENTION",
//          project.LEADING_ZEROS_ON_PROOF_OF_ATTENTION_RELEASE)
//      buildConfigField("String", "BASE_HOST", project.BASE_HOST_PROD)
//      buildConfigField("String", "BACKEND_HOST", project.BACKEND_HOST_PROD)
//      buildConfigField("String", "BDS_BASE_HOST", project.BDS_BASE_HOST_PROD)
//      buildConfigField("String", "MY_APPCOINS_BASE_HOST", project.MY_APPCOINS_BASE_HOST)
//      buildConfigField("String", "LEGACY_PAYMENT_HOST", project.LEGACY_PAYMENT_HOST)
//      buildConfigField("String", "APTOIDE_PKG_NAME", project.APTOIDE_PACKAGE_NAME)
//      buildConfigField("String", "INTERCOM_API_KEY", project.INTERCOM_API_KEY)
//      buildConfigField("String", "INTERCOM_APP_ID", project.INTERCOM_APP_ID)
//      buildConfigField("String", "ADYEN_PUBLIC_KEY", project.ADYEN_PUBLIC_KEY)
//      buildConfigField("String", "SENTRY_DSN_KEY", project.SENTRY_DSN_KEY)
//      buildConfigField("String", "SUBS_BASE_HOST", project.BASE_HOST_PROD)
//      buildConfigField("String", "INDICATIVE_API_KEY", project.INDICATIVE_API_KEY)
//      manifestPlaceholders.legacyPaymentHost = "${project.MANIFEST_LEGACY_PAYMENT_HOST}"
//      manifestPlaceholders.paymentHost = "${project.MANIFEST_PAYMENT_HOST}"
//    }
//    staging {
//      initWith(release)
//      versionNameSuffix = ".staging"
//    }
//    debug {
//      minifyEnabled = false
//      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
//      applicationIdSuffix = ".dev"
//      versionNameSuffix = ".dev"
//      buildConfigField("int", "LEADING_ZEROS_ON_PROOF_OF_ATTENTION",
//          project.property("LEADING_ZEROS_ON_PROOF_OF_ATTENTION_DEBUG"))
//      buildConfigField("String", "BASE_HOST", project.property("BASE_HOST_DEV"))
//      buildConfigField("String", "BACKEND_HOST", project.property("BACKEND_HOST_DEV"))
//      buildConfigField("String", "BDS_BASE_HOST", project.property("BDS_BASE_HOST_DEV"))
//      buildConfigField("String", "MY_APPCOINS_BASE_HOST", project.property("MY_APPCOINS_BASE_HOST_DEV"))
//      buildConfigField("String", "APTOIDE_PKG_NAME", project.property("APTOIDE_PACKAGE_NAME_DEV"))
//      buildConfigField("String", "LEGACY_PAYMENT_HOST", project.property("LEGACY_PAYMENT_HOST_DEV"))
//      buildConfigField("String", "PAYMENT_HOST", project.property("PAYMENT_HOST_DEV"))
//      buildConfigField("String", "INTERCOM_API_KEY", project.property("INTERCOM_API_KEY_DEV"))
//      buildConfigField("String", "INTERCOM_APP_ID", project.property("INTERCOM_APP_ID_DEV"))
//      buildConfigField("String", "ADYEN_PUBLIC_KEY", project.property("ADYEN_PUBLIC_KEY_DEV"))
//      buildConfigField("String", "SENTRY_DSN_KEY", project.property("SENTRY_DSN_KEY_DEV"))
//      buildConfigField("String", "INDICATIVE_API_KEY", project.INDICATIVE_API_KEY_DEV)
//      manifestPlaceholders.legacyPaymentHost = "${project.MANIFEST_LEGACY_PAYMENT_HOST_DEV}"
//      manifestPlaceholders.paymentHost = "${project.MANIFEST_PAYMENT_HOST_DEV}"
//      applicationVariants.all { variant -> renameArtifact(defaultConfig)
//      }
//    }
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//  implementation(project(":legacy:tn"))
  implementation(project(":legacy:airdrop"))
  implementation(project(":legacy:billing"))
  implementation(project(":legacy:commons"))
  implementation(project(":legacy:gamification"))
  implementation(project(":legacy:permissions"))
  implementation(project(":legacy:appcoinsRewards"))
  implementation(project(":legacy:skills"))
  implementation(project(":legacy:ethereumj-android"))

  implementation(libs.kotlin.stdlib)
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
  kapt(libs.androidx.room.compiler)

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

//fun renameArtifact(defaultConfig: DefaultConfig) {
//  android.applicationVariants.all { variant ->
//    variant.outputs.all {
//      val SEP = "_"
//      val buildType = variant.buildType.name
//      val versionName = variant.versionName
//      val versionCode = defaultConfig.versionCode
//      val fileName = "AppCoins_Wallet_v$versionName$SEP$versionCode$SEP$buildType.apk"
//      outputFileName = File(fileName)
//    }
//  }
//}