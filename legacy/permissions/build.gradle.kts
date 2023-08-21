plugins{
  id("appcoins.jvm.library")
}

dependencies {
  implementation(project(":core:utils:jvm-common"))
  implementation(libs.rx.rxjava)
  testImplementation(libs.bundles.testing)
}