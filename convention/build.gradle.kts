plugins { `kotlin-dsl` }

group = "com.appcoins.wallet.convention"


dependencies {
  implementation(libs.gradlePlugin.android)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.hilt)
  implementation(libs.gradlePlugin.ksp)
  implementation(libs.gradlePlugin.junit5)
  implementation(libs.firebase.crashlytics.gradlePlugin)
  implementation(libs.firebase.performance.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("AndroidApp") {
      id = "appcoins.android.app"
      implementationClass = "com.appcoins.wallet.convention.plugins.AndroidAppPlugin"
    }
    register("AndroidFeature") {
      id = "appcoins.android.feature"
      implementationClass = "com.appcoins.wallet.convention.plugins.AndroidFeaturePlugin"
    }
    register("AndroidLibrary") {
      id = "appcoins.android.library"
      implementationClass = "com.appcoins.wallet.convention.plugins.AndroidLibraryPlugin"
    }
    register("AndroidLibraryCompose") {
      id = "appcoins.android.library.compose"
      implementationClass = "com.appcoins.wallet.convention.plugins.AndroidLibraryComposePlugin"
    }
    register("JvmLibrary") {
      id = "appcoins.jvm.library"
      implementationClass = "com.appcoins.wallet.convention.plugins.JvmLibraryPlugin"
    }
    register("Room") {
      id = "appcoins.room"
      implementationClass = "com.appcoins.wallet.convention.plugins.RoomPlugin"
    }
    register("Hilt") {
      id = "appcoins.hilt"
      implementationClass = "com.appcoins.wallet.convention.plugins.HiltPlugin"
    }
    register("JacocoApp") {
      id = "appcoins.jacoco.app"
      implementationClass = "com.appcoins.wallet.convention.plugins.JacocoApplicationPlugin"
    }
    register("JacocoLibrary") {
      id = "appcoins.jacoco.library"
      implementationClass = "com.appcoins.wallet.convention.plugins.JacocoLibraryPlugin"
    }
    register("FirebasePlugin") {
      id = "appcoins.firebase"
      implementationClass = "com.appcoins.wallet.convention.plugins.FirebasePlugin"
    }
  }
}
