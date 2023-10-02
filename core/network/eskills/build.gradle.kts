plugins {
  id("appcoins.android.library")
}

android {
  namespace = "com.appcoins.wallet.core.network.eskills"
}

dependencies {
  implementation(project(":core:network:base"))
  implementation(project(":core:utils:properties"))
  implementation(libs.bundles.network)
  implementation(libs.bundles.rx)
  implementation("io.reactivex:rxjava:1.2.7")
  implementation("com.github.akarnokd:rxjava2-interop:0.13.7")
  implementation(libs.bundles.androidx.room)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.3.7")
  api("com.liulishuo.filedownloader:library:1.4.1")
}