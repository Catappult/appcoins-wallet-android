package com.appcoins.wallet.convention.extensions

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.ApplicationDefaultConfig
import org.gradle.api.Project


val defaultBuildConfigList = mutableListOf(
  BuildConfigField("String", "FLURRY_APK_KEY"),
  BuildConfigField("String", "RAKAM_API_KEY"),
  BuildConfigField("String", "INFURA_API_KEY_MAIN"),
  BuildConfigField("String", "INFURA_API_KEY_ROPSTEN"),
  BuildConfigField("String", "INFURA_API_KEY_RINKEBY"),
  BuildConfigField("String", "AMPLITUDE_API_KEY"),
  BuildConfigField("String", "FEEDBACK_ZENDESK_API_KEY"),
  BuildConfigField("String", "APPSFLYER_KEY"),
)

val debugBuildConfigList = mutableListOf(
  BuildConfigField("String", "INTERCOM_API_KEY", "INTERCOM_API_KEY_DEV"),
  BuildConfigField("String", "INTERCOM_APP_ID", "INTERCOM_APP_ID_DEV"),
  BuildConfigField("String", "ADYEN_PUBLIC_KEY", "ADYEN_PUBLIC_KEY_DEV"),
  BuildConfigField("String", "SENTRY_DSN_KEY", "SENTRY_DSN_KEY_DEV"),
  BuildConfigField("String", "INDICATIVE_API_KEY", "INDICATIVE_API_KEY_DEV"),
)

val releaseBuildConfigList = mutableListOf(
  BuildConfigField("String", "INTERCOM_API_KEY"),
  BuildConfigField("String", "INTERCOM_APP_ID"),
  BuildConfigField("String", "ADYEN_PUBLIC_KEY"),
  BuildConfigField("String", "SENTRY_DSN_KEY"),
  BuildConfigField("String", "INDICATIVE_API_KEY"),
)

internal fun ApplicationDefaultConfig.buildConfigFields(project: Project) {
  for (variable in defaultBuildConfigList) {
    buildConfigField(
      type = variable.type,
      name = variable.name,
      value = if (variable.value != null) project.property(variable.value).toString()
      else project.property(variable.name).toString()
    )
  }
}

internal fun ApplicationBuildType.buildConfigFields(project: Project, type: BuildConfigType) {
  val buildList = when (type) {
    BuildConfigType.RELEASE -> releaseBuildConfigList
    BuildConfigType.DEBUG -> debugBuildConfigList
  }

  for (variable in buildList) {
    buildConfigField(
      type = variable.type,
      name = variable.name,
      value = if (variable.value != null) project.property(variable.value).toString()
      else project.property(variable.name).toString()
    )
  }
}

data class BuildConfigField(val type: String, val name: String, val value: String? = null)

enum class BuildConfigType { RELEASE, DEBUG }