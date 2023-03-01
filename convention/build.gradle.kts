import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
}

group = "com.appcoins.wallet.convention"

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

dependencies {
  implementation(libs.gradlePlugin.android)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.hilt)
  implementation(libs.gradlePlugin.ksp)
  implementation(libs.gradlePlugin.junit5)
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
  }
}