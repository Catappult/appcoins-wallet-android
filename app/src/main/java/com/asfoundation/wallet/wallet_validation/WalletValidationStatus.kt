package com.asfoundation.wallet.wallet_validation

enum class WalletValidationStatus {

  SUCCESS,
  INVALID_INPUT,
  INVALID_PHONE,
  DOUBLE_SPENT,
  GENERIC_ERROR,
  NO_NETWORK,
  REGION_NOT_SUPPORTED,
  LANDLINE_NOT_SUPPORTED

}