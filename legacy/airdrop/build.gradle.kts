plugins{
  id("appcoins.jvm.library")
}
dependencies {
  implementation(libs.bundles.network)
  implementation(libs.jetbrains.annotations)
  testImplementation(libs.bundles.testing)

}