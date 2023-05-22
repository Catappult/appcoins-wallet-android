plugins{
  id("appcoins.android.library")
}

android{
  namespace = "com.appcoins.wallet.feature.backup.ui"
  defaultConfig {
    buildFeatures {
      viewBinding = true
    }
  }
}

dependencies{
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

  implementation(project(":legacy:billing"))
  implementation(project(":feature:wallet-info:data"))
  implementation(project(":feature:backup:data"))
  implementation(project(":feature:change-currency:data"))
  implementation(project(":ui:widgets"))
  implementation(project(":ui:common"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:analytics"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:arch"))
  implementation(project(":core:legacy-base"))

  implementation(libs.bundles.rx)
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.google.material)
  implementation(libs.google.gson)

}