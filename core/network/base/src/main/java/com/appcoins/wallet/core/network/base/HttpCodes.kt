package com.appcoins.wallet.core.network.base

object HttpCodes {

  const val INFO_START_CODE = 100
  const val INFO_END_CODE = 199

  const val SUCCESS_START_CODE = 200
  const val SUCCESS_END_CODE = 299

  const val REDIRECT_START_CODE = 300
  const val REDIRECT_END_CODE = 399

  const val CLIENT_ERROR_START_CODE = 400
  const val CLIENT_ERROR_END_CODE = 499

  const val SERVER_ERROR_START_CODE = 500
  const val SERVER_ERROR_END_CODE = 599

  fun isInfo(code: Int): Boolean = code in INFO_START_CODE..INFO_END_CODE
  fun isSuccess(code: Int): Boolean = code in SUCCESS_START_CODE..SUCCESS_END_CODE
  fun isRedirect(code: Int): Boolean = code in REDIRECT_START_CODE..REDIRECT_END_CODE
  fun isClientError(code: Int): Boolean = code in CLIENT_ERROR_START_CODE..CLIENT_ERROR_END_CODE
  fun isServerError(code: Int): Boolean = code in SERVER_ERROR_START_CODE..SERVER_ERROR_END_CODE
}