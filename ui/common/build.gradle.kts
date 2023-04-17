plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.ui.common"
  defaultConfig {
    buildFeatures {
      composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
      }
      compose = true
    }
  }
}

dependencies {
  implementation(libs.google.material)
  implementation(libs.bundles.adyen) {
    exclude(group = "io.michaelrocks", module = "paranoid-core")
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }
  implementation(libs.bundles.androidx.compose)
}