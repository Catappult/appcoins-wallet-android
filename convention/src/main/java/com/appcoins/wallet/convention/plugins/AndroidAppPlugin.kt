package com.appcoins.wallet.convention.plugins

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifactsLoader
import com.appcoins.wallet.convention.Config
import com.appcoins.wallet.convention.extensions.BuildConfigType
import com.appcoins.wallet.convention.extensions.buildConfigFields
import com.appcoins.wallet.convention.extensions.configureAndroidAndKotlin
import com.appcoins.wallet.convention.transforms.PathParserNullFixFactory
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault
abstract class ApkRenameTask : DefaultTask() {
  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val apkFolder: DirectoryProperty

  @get:OutputDirectory
  abstract val outputFolder: DirectoryProperty

  @get:Input
  abstract val buildTypeName: Property<String>

  @get:Internal
  abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

  @TaskAction
  fun rename() {
    val artifacts = builtArtifactsLoader.get().load(apkFolder.get()) ?: return
    val sep = "_"
    val bt = buildTypeName.get()
    artifacts.elements.forEach { artifact ->
      val vn = artifact.versionName ?: "unknown"
      val vc = artifact.versionCode ?: 0
      val source = File(artifact.outputFile)
      source.copyTo(File(outputFolder.get().asFile, "AppCoins_Wallet_v$vn${sep}$vc${sep}$bt.apk"), overwrite = true)
    }
  }
}

class AndroidAppPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("org.jetbrains.kotlin.plugin.compose")
        apply("kotlin-parcelize")
        apply<JacocoApplicationPlugin>()
      }

      extensions.configure(JavaPluginExtension::class.java) {
        sourceCompatibility = Config.jvm.javaVersion
        targetCompatibility = Config.jvm.javaVersion
      }

      // Patch PathParser.deepCopyNodes to restore the null-safe behaviour removed in
      // androidx.core 1.13.0 — needed for compatibility with VK SDK rich-vector library.
      extensions.configure<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
          variant.instrumentation.transformClassesWith(PathParserNullFixFactory::class.java, InstrumentationScope.ALL) {}
          val buildType = variant.buildType ?: ""
          val taskName = "rename${variant.name.replaceFirstChar(Char::uppercase)}Apk"
          tasks.register(taskName, ApkRenameTask::class.java) {
            buildTypeName.set(buildType)
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
            outputFolder.set(layout.buildDirectory.dir("outputs/apk_renamed/${variant.name}"))
          }
        }
      }

      extensions.configure<ApplicationExtension> {
        configureAndroidAndKotlin(this)
        ndkVersion = Config.android.ndkVersion
        defaultConfig {
          val giftCardHost = "giftcard"
          val promoCodeHost = "promocode"
          targetSdk = Config.android.targetSdk
          multiDexEnabled = true
          lint {
            abortOnError = false
          }
          buildConfigFields(project, rootDir)
          javaCompileOptions {
            annotationProcessorOptions {
              annotationProcessorOptions.arguments["room.schemaLocation"] =
                "${project.projectDir}/schemas"
            }
          }

          buildConfigField("String", "PROMO_CODE_HOST", "\"$promoCodeHost\"")
          manifestPlaceholders["promoCodeHost"] = promoCodeHost

          buildConfigField("String", "GIFT_CARD_HOST", "\"$giftCardHost\"")
          manifestPlaceholders["giftCardHost"] = giftCardHost
          manifestPlaceholders["VkExternalAuthRedirectHost"] =
            project.property("VK_EXTERNAL_AUTH_REDIRECT_HOST").toString()
        }

        signingConfigs {
          register("release") {
            storeFile = project.property("BDS_WALLET_STORE_FILE")?.let { file(it) }
            storePassword = project.property("BDS_WALLET_STORE_PASSWORD").toString()
            keyAlias = project.property("BDS_WALLET_KEY_ALIAS").toString()
            keyPassword = project.property("BDS_WALLET_KEY_PASSWORD").toString()
          }
          register("dev") {
            storeFile = project.property("BDS_WALLET_DEV_STORE_FILE")?.let { file(it) }
            storePassword = project.property("BDS_WALLET_DEV_STORE_PASSWORD").toString()
            keyAlias = project.property("BDS_WALLET_DEV_KEY_ALIAS").toString()
            keyPassword = project.property("BDS_WALLET_DEV_KEY_PASSWORD").toString()
          }
        }

        buildTypes {
          debug {
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = ".dev"
            signingConfig = signingConfigs.getByName("dev")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.DEBUG)
            manifestPlaceholders["legacyPaymentHost"] =
              project.property("MANIFEST_LEGACY_PAYMENT_HOST_DEV").toString()
            manifestPlaceholders["paymentHost"] =
              project.property("MANIFEST_PAYMENT_HOST_DEV").toString()
            manifestPlaceholders["VkExternalAuthRedirectScheme"] =
              project.property("VK_EXTERNAL_AUTH_REDIRECT_BUILD_SCHEME_DEV").toString()
            resValue("string", "com_vk_sdk_AppId", project.property("VK_SDK_APP_ID_DEV").toString())
            resValue(
              "string",
              "vk_client_secret",
              project.property("VK_CLIENT_SECRET_DEV").toString()
            )
            resValue(
              "string",
              "vk_external_oauth_redirect_url",
              project.property("VK_EXTERNAL_URL_REDIRECT_DEV").toString()
            )
          }

          release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigFields(project, BuildConfigType.RELEASE)
            manifestPlaceholders["legacyPaymentHost"] =
              project.property("MANIFEST_LEGACY_PAYMENT_HOST").toString()
            manifestPlaceholders["paymentHost"] =
              project.property("MANIFEST_PAYMENT_HOST").toString()
            manifestPlaceholders["VkExternalAuthRedirectScheme"] =
              project.property("VK_EXTERNAL_AUTH_REDIRECT_BUILD_SCHEME").toString()
            resValue("string", "com_vk_sdk_AppId", project.property("VK_SDK_APP_ID").toString())
            resValue("string", "vk_client_secret", project.property("VK_CLIENT_SECRET").toString())
            resValue(
              "string",
              "vk_external_oauth_redirect_url",
              project.property("VK_EXTERNAL_URL_REDIRECT").toString()
            )
          }

        }

        buildFeatures {
          buildConfig = true
          resValues = true
          viewBinding = true
          compose = true
          aidl = true
        }

        flavorDimensions.add(Config.distributionFlavorDimension)
        productFlavors {
          create(Config.googlePlayDistribution) {
            dimension = Config.distributionFlavorDimension
          }
          create(Config.aptoidePlayDistribution) {
            dimension = Config.distributionFlavorDimension
          }
        }

      }
    }
  }
}


