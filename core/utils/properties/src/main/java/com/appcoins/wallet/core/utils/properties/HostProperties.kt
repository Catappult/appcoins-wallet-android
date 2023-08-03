package com.appcoins.wallet.core.utils.properties

object HostProperties {
  val BACKEND_HOST = if (BuildConfig.DEBUG) BACKEND_HOST_DEV else BACKEND_HOST_PROD
  val MS_HOST = if (BuildConfig.DEBUG) BASE_HOST_DEV else BASE_HOST_PROD
  val SKILLS_HOST = if (BuildConfig.DEBUG) BASE_HOST_SKILLS_DEV else BASE_HOST_SKILLS_PROD
  val WS75_HOST = if (BuildConfig.DEBUG) BDS_BASE_HOST_DEV else BDS_BASE_HOST_PROD
  val MY_APPCOINS_HOST = if (BuildConfig.DEBUG) MY_APPCOINS_BASE_HOST_DEV else MY_APPCOINS_BASE_HOST
  val TRANSACTION_DETAILS_HOST =
    if (BuildConfig.DEBUG) TRANSACTION_DETAILS_HOST_ROPSTEN else TRANSACTION_DETAILS_HOST_MAIN
  const val BACKEND_HOST_NAME_PROD = "apichain.catappult.io"
  const val BACKEND_HOST_NAME_DEV = "apichain.dev.catappult.io"
  const val APTOIDE_WEB_SERVICES_AB_TEST_HOST = "https://abtest.aptoide.com/api/v1/"
  const val FEEDBACK_ZENDESK_BASE_HOST = "https://aptoide.zendesk.com/api/v2/"
}

private const val BASE_HOST_PROD = "https://api.catappult.io"
private const val BASE_HOST_DEV = "https://api.dev.catappult.io"
private const val BACKEND_HOST_PROD = "https://apichain.catappult.io"
private const val BACKEND_HOST_DEV = "https://apichain.dev.catappult.io"
private const val BASE_HOST_SKILLS_PROD = "https://api.eskills.catappult.io"
private const val BASE_HOST_SKILLS_DEV = "https://api.eskills.dev.catappult.io"
private const val BDS_BASE_HOST_PROD = "https://ws75-secondary.aptoide.com/api/"
private const val BDS_BASE_HOST_DEV = "https://ws75-devel.aptoide.com/api/"
private const val MY_APPCOINS_BASE_HOST = "https://myappcoins.com/"
private const val MY_APPCOINS_BASE_HOST_DEV = "https://dev.myappcoins.com/"
private const val TRANSACTION_DETAILS_HOST_MAIN = "https://appcexplorer.io/transaction/"
private const val TRANSACTION_DETAILS_HOST_ROPSTEN = "https://ropsten.appcexplorer.io/transaction/"
