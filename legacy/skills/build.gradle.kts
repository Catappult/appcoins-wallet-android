import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
  id("appcoins.android.library")
}

android {
  namespace = "cm.aptoide.skills"
  buildTypes {
    release {
      buildConfigField(
        "String", "BASE_HOST_SKILLS", project.property("BASE_HOST_SKILLS_PROD").toString()
      )
    }

    debug {
      buildConfigField(
        "String", "BASE_HOST_SKILLS", project.property("BASE_HOST_SKILLS_DEV").toString()
      )
    }
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompact)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.google.material)
  implementation(libs.bundles.rx)
  implementation(libs.bundles.network)

  implementation(libs.lottie)
  implementation(libs.shimmer)
  implementation(libs.web3j) {
    // To resolve the bouncycastle version conflict with the adyen (1.68 vs 1.69)
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
  }

  testImplementation(libs.bundles.testing)
  androidTestImplementation(libs.test.junit.ext)
}