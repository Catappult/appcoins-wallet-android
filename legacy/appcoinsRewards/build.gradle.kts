plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.appcoinsrewards"

  lint {
    disable.add("NullSafeMutableLiveData")
  }
}

dependencies {
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":legacy:bdsbilling"))
  implementation(project(":core:network:microservices"))

  implementation(libs.bundles.network)
  testImplementation(libs.bundles.testing)
}