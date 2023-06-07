import groovy.json.JsonSlurper

plugins {
  id("appcoins.android.library")
}
android {
  namespace = "com.appcoins.wallet.core.utils.partners"
  defaultConfig {
    val inputFile = File("$rootDir/appcoins-services.json")
    val json = JsonSlurper().parseText(inputFile.readText()) as Map<*, *>
    buildConfigField(
      "String",
      "DEFAULT_OEM_ADDRESS",
      "\"" + (json["oems"] as Map<*, *>)["default_address"] + "\""
    )
    buildConfigField(
      "String",
      "DEFAULT_STORE_ADDRESS",
      "\"" + (json["stores"] as Map<*, *>)["default_address"] + "\""
    )
  }

}
dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:properties"))
  implementation(libs.web3j)
  implementation(libs.bundles.rx)
  implementation(libs.network.retrofit)
}