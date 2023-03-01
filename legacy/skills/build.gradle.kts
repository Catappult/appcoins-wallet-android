import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
  id("appcoins.android.library")
  id("appcoins.hilt")
}

android {
  defaultConfig {
//    versionCode 1
//    versionName "1.0"
//    consumerProguardFiles = "consumer-rules.pro"
  }

//  buildTypes {
//    release {
//      minifyEnabled false
//      proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//      buildConfigField "String", "BASE_HOST_SKILLS", project.BASE_HOST_SKILLS_PROD
//      buildConfigField "String", "BASE_HOST", project.BACKEND_HOST_PROD
//      buildConfigField "String", "WALLET_PACKAGE", project.WALLET_PACKAGE_PROD
//    }
//    staging {
//      initWith release
//    }
//    debug {
//      minifyEnabled false
//      proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//      buildConfigField "String", "BASE_HOST_SKILLS", project.BASE_HOST_SKILLS_DEV
//      buildConfigField "String", "BASE_HOST", project.BACKEND_HOST_DEV
//      buildConfigField "String", "WALLET_PACKAGE", project.WALLET_PACKAGE_DEV
//    }
//  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(libs.kotlin.stdlib)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompact)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.google.material)
  implementation(libs.bundles.rx)
  implementation(libs.bundles.network)

  implementation(libs.bundles.hilt.implementation)
  kapt(libs.bundles.hilt.kapt)

  implementation(libs.lottie)
  implementation(libs.shimmer)
  implementation(libs.web3j) {
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }

  testImplementation(libs.bundles.testing)
  androidTestImplementation(libs.test.junit.ext)
}