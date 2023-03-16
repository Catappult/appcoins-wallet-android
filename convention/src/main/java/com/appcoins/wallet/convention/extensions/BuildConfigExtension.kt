package com.appcoins.wallet.convention.extensions

import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.ApplicationDefaultConfig
import org.gradle.api.Project


val defaultBuildConfigList = mutableListOf(
  BuildConfigField("int", "DB_VERSION"),
  BuildConfigField("int", "BILLING_SUPPORTED_VERSION"),
  BuildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_NAME"),
  BuildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_SYMBOL"),
  BuildConfigField("String", "ROPSTEN_DEFAULT_TOKEN_ADDRESS"),
  BuildConfigField("int", "ROPSTEN_DEFAULT_TOKEN_DECIMALS"),
  BuildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_NAME"),
  BuildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL"),
  BuildConfigField("String", "MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS"),
  BuildConfigField("int", "MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS"),
  BuildConfigField("String", "PAYMENT_GAS_LIMIT"),
  BuildConfigField("String", "FLURRY_APK_KEY"),
  BuildConfigField("String", "PAYMENT_HOST_ROPSTEN_NETWORK", "PAYMENT_HOST_DEV"),
  BuildConfigField("String", "PAYMENT_HOST"),
  BuildConfigField("String", "TRANSACTION_DETAILS_HOST"),
  BuildConfigField("String", "TRANSACTION_DETAILS_HOST_ROPSTEN"),
  BuildConfigField("String", "RAKAM_BASE_HOST"),
  BuildConfigField("String", "RAKAM_API_KEY"),
  BuildConfigField("String", "APTOIDE_WEB_SERVICES_AB_TEST_HOST"),
  BuildConfigField("String", "INFURA_API_KEY_MAIN"),
  BuildConfigField("String", "INFURA_API_KEY_ROPSTEN"),
  BuildConfigField("String", "INFURA_API_KEY_RINKEBY"),
  BuildConfigField("String", "AMPLITUDE_API_KEY"),
  BuildConfigField("String", "FEEDBACK_ZENDESK_API_KEY"),
  BuildConfigField("String", "FEEDBACK_ZENDESK_BASE_HOST"),
  BuildConfigField("String", "APTOIDE_TOP_APPS_URL"),
  BuildConfigField("String", "VIP_PROGRAM_BADGE_URL"),
  BuildConfigField("String", "APPSFLYER_KEY"),
  BuildConfigField("String", "TERMS_CONDITIONS_URL"),
  BuildConfigField("String", "PRIVACY_POLICY_URL"),
  BuildConfigField("String", "GAMESHUB_PACKAGE"),
  BuildConfigField("String", "GAMESHUB_OEMID")
)

val debugBuildConfigList = mutableListOf(
  BuildConfigField(
    "int", "LEADING_ZEROS_ON_PROOF_OF_ATTENTION",
    "LEADING_ZEROS_ON_PROOF_OF_ATTENTION_DEBUG"
  ),
  BuildConfigField("String", "BASE_HOST", "BASE_HOST_DEV"),
  BuildConfigField("String", "BACKEND_HOST", "BACKEND_HOST_DEV"),
  BuildConfigField("String", "BDS_BASE_HOST", "BDS_BASE_HOST_DEV"),
  BuildConfigField("String", "MY_APPCOINS_BASE_HOST", "MY_APPCOINS_BASE_HOST_DEV"),
  BuildConfigField("String", "APTOIDE_PKG_NAME", "APTOIDE_PACKAGE_NAME_DEV"),
  BuildConfigField("String", "LEGACY_PAYMENT_HOST", "LEGACY_PAYMENT_HOST_DEV"),
  BuildConfigField("String", "PAYMENT_HOST", "PAYMENT_HOST_DEV"),
  BuildConfigField("String", "INTERCOM_API_KEY", "INTERCOM_API_KEY_DEV"),
  BuildConfigField("String", "INTERCOM_APP_ID", "INTERCOM_APP_ID_DEV"),
  BuildConfigField("String", "ADYEN_PUBLIC_KEY", "ADYEN_PUBLIC_KEY_DEV"),
  BuildConfigField("String", "SENTRY_DSN_KEY", "SENTRY_DSN_KEY_DEV"),
  BuildConfigField("String", "INDICATIVE_API_KEY", "INDICATIVE_API_KEY_DEV"),
  BuildConfigField("String", "BASE_HOST_SKILLS", "BASE_HOST_SKILLS_DEV")
)

val releaseBuildConfigList = mutableListOf(
  BuildConfigField(
    "int", "LEADING_ZEROS_ON_PROOF_OF_ATTENTION",
    "LEADING_ZEROS_ON_PROOF_OF_ATTENTION_RELEASE"
  ),
  BuildConfigField("String", "BASE_HOST", "BASE_HOST_PROD"),
  BuildConfigField("String", "BACKEND_HOST", "BACKEND_HOST_PROD"),
  BuildConfigField("String", "BDS_BASE_HOST", "BDS_BASE_HOST_PROD"),
  BuildConfigField("String", "MY_APPCOINS_BASE_HOST", "MY_APPCOINS_BASE_HOST_DEV"),
  BuildConfigField("String", "LEGACY_PAYMENT_HOST"),
  BuildConfigField("String", "APTOIDE_PKG_NAME", "APTOIDE_PACKAGE_NAME_DEV"),
  BuildConfigField("String", "INTERCOM_API_KEY"),
  BuildConfigField("String", "INTERCOM_APP_ID"),
  BuildConfigField("String", "ADYEN_PUBLIC_KEY"),
  BuildConfigField("String", "SENTRY_DSN_KEY"),
  BuildConfigField("String", "SUBS_BASE_HOST", "BASE_HOST_PROD"),
  BuildConfigField("String", "INDICATIVE_API_KEY"),
  BuildConfigField("String", "BASE_HOST_SKILLS", "BASE_HOST_SKILLS_PROD")
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