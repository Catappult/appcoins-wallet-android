plugins {
  id("appcoins.android.library")
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

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)

  implementation(libs.zxing.android)
  implementation(libs.glide)
}
