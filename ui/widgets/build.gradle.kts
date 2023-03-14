plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.ui.widgets"
  buildTypes {
    release {
    }
    debug {
    }
  }
}

dependencies {
//  implementation(project(":ui:commons"))
//  implementation(project(":core:utils"))

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)

  implementation(libs.zxing.android)
  implementation(libs.glide)
  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)
  implementation(libs.viewbinding.delegate)
}
