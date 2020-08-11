package com.asfoundation.wallet.wallet_validation

enum class WalletValidationStatus {

  SUCCESS,
  INVALID_INPUT,
  INVALID_PHONE,
  INVALID_CODE,
  DOUBLE_SPENT,
  GENERIC_ERROR,
  NO_NETWORK,
  REGION_NOT_SUPPORTED,
  LANDLINE_NOT_SUPPORTED,
  EXPIRED_CODE,
  TOO_MANY_ATTEMPTS

}