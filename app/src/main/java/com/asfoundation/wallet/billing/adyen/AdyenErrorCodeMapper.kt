package com.asfoundation.wallet.billing.adyen

import androidx.annotation.StringRes
import com.asf.wallet.R
import javax.inject.Inject

class AdyenErrorCodeMapper @Inject constructor() {

  @StringRes
  internal fun map(errorCode: Int): Int {
    return when (errorCode) {
      DECLINED, BLOCKED_CARD, TRANSACTION_NOT_PERMITTED, REVOCATION_OF_AUTH, DECLINED_NON_GENERIC, ISSUER_SUSPECTED_FRAUD -> R.string.purchase_card_error_general_2
      REFERRAL, ACQUIRER_ERROR, ISSUER_UNAVAILABLE -> R.string.purchase_card_error_general_1
      EXPIRED_CARD -> R.string.purchase_card_error_expired
      INVALID_AMOUNT, NOT_ENOUGH_BALANCE, WITHDRAW_AMOUNT_EXCEEDED, RESTRICTED_CARD -> R.string.purchase_card_error_no_funds
      INVALID_CARD_NUMBER -> R.string.purchase_card_error_invalid_details
      NOT_SUPPORTED -> R.string.purchase_card_error_not_supported
      INCORRECT_ONLINE_PIN, PIN_TRIES_EXCEEDED -> R.string.purchase_error_wrong_pin_body
      NOT_3D_AUTHENTICATED -> R.string.purchase_error_3d_body
      FRAUD, CANCELLED_DUE_TO_FRAUD -> R.string.purchase_card_error_general_2
      else -> R.string.purchase_error_generic_contact_us_body
    }
  }

  internal fun needsCardRepeat(errorCode: Int): Boolean {
    return when (errorCode) {
      INVALID_CARD_NUMBER, INCORRECT_ONLINE_PIN, PIN_TRIES_EXCEEDED -> true
      else -> false
    }
  }

  companion object {

    const val DECLINED = 2
    const val REFERRAL = 3
    const val ACQUIRER_ERROR = 4
    const val BLOCKED_CARD = 5
    const val EXPIRED_CARD = 6
    const val INVALID_AMOUNT = 7
    const val INVALID_CARD_NUMBER = 8
    const val ISSUER_UNAVAILABLE = 9
    const val NOT_SUPPORTED = 10
    const val NOT_3D_AUTHENTICATED = 11
    const val NOT_ENOUGH_BALANCE = 12
    const val INCORRECT_ONLINE_PIN = 17
    const val PIN_TRIES_EXCEEDED = 18
    const val FRAUD = 20
    const val CANCELLED_DUE_TO_FRAUD = 22
    const val TRANSACTION_NOT_PERMITTED = 23
    const val CVC_DECLINED = 24
    const val RESTRICTED_CARD = 25
    const val REVOCATION_OF_AUTH = 26
    const val DECLINED_NON_GENERIC = 27
    const val WITHDRAW_AMOUNT_EXCEEDED = 28
    const val ISSUER_SUSPECTED_FRAUD = 31

  }

}
