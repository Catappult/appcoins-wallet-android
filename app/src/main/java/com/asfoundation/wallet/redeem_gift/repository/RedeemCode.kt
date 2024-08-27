package com.asfoundation.wallet.redeem_gift.repository

sealed class RedeemCode

object SuccessfulRedeem : RedeemCode()

sealed class FailedRedeem : RedeemCode() {
  object AlreadyRedeemedError : FailedRedeem()  // 450
  object OnlyNewUsersError : FailedRedeem()     // 451
  object GenericError : FailedRedeem()  // all others
}