plugins {
  id("appcoins.android.library")
}

android {
  compileSdk = 31
//  compileSdkVersion project.compile_sdk_version

  defaultConfig {
//    minSdkVersion project.min_sdk_version
//    targetSdkVersion project.target_sdk_version
//    versionCode = 1
//    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//    lintOptions {
//      abortOnError = false
//    }
  }

//  buildTypes {
//    release {
//      minifyEnabled false
//      proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
//      buildConfigField "String", "BASE_HOST", project.BASE_HOST_PROD
//      buildConfigField "int", "NETWORK_ID", project.NETWORK_ID_PROD
//    }
//    staging {
//      initWith release
//    }
//    debug {
//      minifyEnabled false
//      buildConfigField "String", "BASE_HOST", project.BASE_HOST_DEV
//      buildConfigField "int", "NETWORK_ID", project.NETWORK_ID_DEV
//    }
//  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  api(project(":legacy:bdsbilling"))
  implementation(project(":legacy:commons"))

  implementation(libs.bundles.rx)
  implementation(libs.bundles.network)
  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
  implementation(libs.bundles.jackson)
  implementation(libs.spongycastle.core)
  testImplementation(libs.bundles.testing)
}
