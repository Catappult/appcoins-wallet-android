plugins {
  id("appcoins.android.library")
}

android {
  defaultConfig {
//    lintOptions {
//      abortOnError = false
//    }
  }

  buildTypes {
    release {
      buildConfigField("int", "NETWORK_ID", project.property("NETWORK_ID_PROD").toString())
    }
    debug {
      buildConfigField("int", "NETWORK_ID", project.property("NETWORK_ID_DEV").toString())
    }
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  api(project(":legacy:bdsbilling"))
  implementation(project(":legacy:commons"))

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
}
