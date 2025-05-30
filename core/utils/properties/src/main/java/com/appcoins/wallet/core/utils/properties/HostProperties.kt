package com.appcoins.wallet.core.utils.properties

object HostProperties {
  val BACKEND_HOST = if (BuildConfig.DEBUG) BACKEND_HOST_DEV else BACKEND_HOST_PROD
  val MS_HOST = if (BuildConfig.DEBUG) BASE_HOST_DEV else BASE_HOST_PROD
  val PAY_FLOW_HOST = if (BuildConfig.DEBUG) PAY_FLOW_HOST_DEV else PAY_FLOW_HOST_PROD
  val SKILLS_HOST = if (BuildConfig.DEBUG) BASE_HOST_SKILLS_DEV else BASE_HOST_SKILLS_PROD
  val MY_APPCOINS_HOST = if (BuildConfig.DEBUG) MY_APPCOINS_BASE_HOST_DEV else MY_APPCOINS_BASE_HOST
  val TRANSACTION_DETAILS_HOST =
    if (BuildConfig.DEBUG) TRANSACTION_DETAILS_HOST_ROPSTEN else TRANSACTION_DETAILS_HOST_MAIN
  const val BACKEND_HOST_NAME_PROD = "apichain.catappult.io"
  const val BACKEND_HOST_NAME_DEV = "apichain.dev.catappult.io"
  const val APTOIDE_WEB_SERVICES_AB_TEST_HOST = "https://abtest.aptoide.com/api/v1/"
  const val FEEDBACK_ZENDESK_BASE_HOST = "https://aptoide.zendesk.com/api/v2/"
  val AMAZON_PAY_REDIRECT_BASE_URL = if (BuildConfig.DEBUG) AMAZON_PAY_REDIRECT_BASE_URL_DEV else AMAZON_PAY_REDIRECT_BASE_URL_PROD
  val WEBVIEW_PAYMENT_URL = if (BuildConfig.DEBUG) WEBVIEW_PAYMENT_URL_DEV else WEBVIEW_PAYMENT_URL_PROD
  val WEBVIEW_LOGIN_URL = if (BuildConfig.DEBUG) WEBVIEW_LOGIN_URL_DEV else WEBVIEW_LOGIN_URL_PROD
  const val FLAGR_BASE_HOST = "https://flagr.aptoide.com/api/v1/"
}

private const val BASE_HOST_PROD = "https://api.catappult.io"
private const val BASE_HOST_DEV = "https://api.dev.catappult.io"
private const val PAY_FLOW_HOST_PROD = "https://payflowsdk.aptoide.com/api"
private const val PAY_FLOW_HOST_DEV = "https://payflowsdk.dev.aptoide.com/api"
private const val BACKEND_HOST_PROD = "https://apichain.catappult.io"
private const val BACKEND_HOST_DEV = "https://apichain.dev.catappult.io"
private const val BASE_HOST_SKILLS_PROD = "https://api.eskills.catappult.io"
private const val BASE_HOST_SKILLS_DEV = "https://api.eskills.dev.catappult.io"
private const val MY_APPCOINS_BASE_HOST = "https://myappcoins.com/"
private const val MY_APPCOINS_BASE_HOST_DEV = "https://dev.myappcoins.com/"
private const val TRANSACTION_DETAILS_HOST_MAIN = "https://appcexplorer.io/transaction/"
private const val TRANSACTION_DETAILS_HOST_ROPSTEN = "https://ropsten.appcexplorer.io/transaction/"
private const val AMAZON_PAY_REDIRECT_BASE_URL_DEV = "https://apichain.dev.catappult.io/amazonpay/result"
private const val AMAZON_PAY_REDIRECT_BASE_URL_PROD = "https://apichain.catappult.io/amazonpay/result"
private const val WEBVIEW_PAYMENT_URL_PROD = "https://developers.catappult.io/iap"
private const val WEBVIEW_PAYMENT_URL_DEV = "https://wallet.dev.appcoins.io/iap"
private const val WEBVIEW_LOGIN_URL_PROD = "https://wallet.aptoide.com/en/wallet/sign-in"
private const val WEBVIEW_LOGIN_URL_DEV = "https://wallet.dev.aptoide.com/en/wallet/sign-in"
