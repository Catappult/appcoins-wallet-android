plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.networkbase"

  defaultConfig {
    //TODO change these config fields approach
    buildConfigField("String", "APPLICATION_ID", "\"com.appcoins.wallet\"")
    buildConfigField("String", "VERSION_CODE", "\"258\"")
    buildConfigField("String", "VERSION_NAME", "\"2.9.1.0\"")
  }
}

dependencies {
  implementation(libs.bundles.network)
  implementation(libs.appcoins.sdk)
  implementation(project(":legacy:commons"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:common"))
}
