plugins{
  `kotlin-dsl`
}

group = "com.appcoins.wallet.plugins"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(libs.gradlePlugin.android)
  implementation(libs.gradlePlugin.kotlin)
  implementation(libs.gradlePlugin.hilt)
  implementation(libs.gradlePlugin.ksp)
}

gradlePlugin {
  plugins {
    register("AndroidApp") {
      id = "appcoins.android.app"
      implementationClass = "com.appcoins.wallet.plugins.convention.AndroidAppPlugin"
    }
    register("AndroidFeature") {
      id = "appcoins.android.feature"
      implementationClass = "com.appcoins.wallet.plugins.convention.AndroidFeaturePlugin"
    }
    register("AndroidLib") {
      id = "appcoins.android.library"
      implementationClass = "com.appcoins.wallet.plugins.convention.AndroidLibPlugin"
    }
  }
}