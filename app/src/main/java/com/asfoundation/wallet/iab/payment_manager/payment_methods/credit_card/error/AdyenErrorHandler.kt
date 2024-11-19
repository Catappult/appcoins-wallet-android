package com.asfoundation.wallet.iab.payment_manager.payment_methods.credit_card.error

import androidx.annotation.StringRes
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.ALREADY_PROCESSED
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CARD_SECURITY_VALIDATION
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CURRENCY_NOT_SUPPORTED
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.INVALID_CARD
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.INVALID_COUNTRY_CODE
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.OUTDATED_CARD
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.PAYMENT_ERROR
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.TRANSACTION_AMOUNT_EXCEEDED
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.ui.common.StringProvider
import com.appcoins.wallet.ui.common.callAsync
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.service.ServicesErrorCodeMapper

class AdyenErrorHandlerImpl(
  private val stringProvider: StringProvider,
  private val paymentType: String,
  private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val servicesErrorCodeMapper: ServicesErrorCodeMapper,
) : AdyenErrorHandler {

  private val verificationType by lazy {
    if (paymentType == PaymentType.CARD.name) {
      VerificationType.CREDIT_CARD
    } else {
      VerificationType.PAYPAL
    }
  }

  override fun isNetworkError(paymentModel: PaymentModel) =
    paymentModel.error.isNetworkError

  override fun isCVCError(paymentModel: PaymentModel): Boolean =
    paymentModel.refusalCode == AdyenErrorCodeMapper.CVC_DECLINED ||
        paymentModel.error.errorInfo?.errorType == ErrorInfo.ErrorType.CVC_LENGTH

  override fun isCVCRequired(paymentModel: PaymentModel): Boolean =
    paymentModel.error.errorInfo?.errorType == ErrorInfo.ErrorType.CVC_REQUIRED

  override fun shouldShowVerifyScreen(paymentModel: PaymentModel): Boolean =
    paymentModel.refusalCode == AdyenErrorCodeMapper.FRAUD

  override suspend fun handleError(paymentModel: PaymentModel): String =
    stringProvider.getString(
      when {
        paymentModel.refusalReason != null -> {
          if (paymentModel.refusalCode == AdyenErrorCodeMapper.FRAUD) {
            handleFraudFlow(adyenErrorCodeMapper.map(paymentModel.refusalCode!!))
          } else {
            handleErrors(paymentModel.error)
          }
        }

        paymentModel.error.hasError ->
          handleErrors(paymentModel.error, paymentModel.refusalCode)

        paymentModel.status == PaymentModel.Status.FAILED && paymentType == PaymentType.PAYPAL.name ->
          retrieveFailedReason(paymentModel.uid)

        else -> R.string.unknown_error
      }
    )

  private suspend fun retrieveFailedReason(uid: String): Int =
    try {
      val reason = adyenPaymentInteractor.getFailedTransactionReason(uid).callAsync()
      if (reason.errorCode != null) {
        adyenErrorCodeMapper.map(reason.errorCode!!)
      } else {
        R.string.unknown_error
      }
    } catch (e: Throwable) {
      R.string.unknown_error
    }


  private suspend fun handleFraudFlow(@StringRes errorCode: Int): Int =
    try {
      val isVerified = adyenPaymentInteractor.isWalletVerified(verificationType).callAsync()

      if (isVerified)
        R.string.purchase_error_verify_card
      else
        R.string.purchase_error_verify_wallet
    } catch (e: Throwable) {
      errorCode
    }

  private suspend fun handleErrors(error: Error, code: Int? = null): Int {
    return when (error.errorInfo?.errorType) {
      INVALID_CARD -> R.string.purchase_error_invalid_credit_card
      CARD_SECURITY_VALIDATION -> R.string.purchase_error_card_security_validation
      OUTDATED_CARD -> R.string.purchase_card_error_re_insert
      ALREADY_PROCESSED -> R.string.purchase_error_card_already_in_progress
      PAYMENT_ERROR -> R.string.purchase_error_payment_rejected
      INVALID_COUNTRY_CODE -> R.string.unknown_error
      PAYMENT_NOT_SUPPORTED_ON_COUNTRY -> R.string.purchase_error_payment_rejected
      CURRENCY_NOT_SUPPORTED -> R.string.purchase_card_error_general_1
      TRANSACTION_AMOUNT_EXCEEDED -> R.string.purchase_card_error_no_funds
      else -> if (error.errorInfo?.httpCode != null) {
        val resId = servicesErrorCodeMapper.mapError(error.errorInfo?.errorType)
        if (error.errorInfo?.httpCode == 403)
          handleFraudFlow(resId)
        else
          resId
      } else {
        adyenErrorCodeMapper.map(code ?: 0)
      }
    }
  }
}

interface AdyenErrorHandler {
  fun isNetworkError(paymentModel: PaymentModel): Boolean

  fun isCVCRequired(paymentModel: PaymentModel): Boolean

  fun isCVCError(paymentModel: PaymentModel): Boolean

  fun shouldShowVerifyScreen(paymentModel: PaymentModel): Boolean

  suspend fun handleError(paymentModel: PaymentModel): String
}
