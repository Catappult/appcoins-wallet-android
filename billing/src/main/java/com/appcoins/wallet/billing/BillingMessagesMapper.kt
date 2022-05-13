package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.exceptions.ApiException
import com.appcoins.wallet.bdsbilling.exceptions.BillingException
import com.appcoins.wallet.bdsbilling.exceptions.ServiceUnavailableException
import com.appcoins.wallet.bdsbilling.exceptions.UnknownException
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.INAPP_DATA_SIGNATURE
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.INAPP_PURCHASE_DATA
import com.appcoins.wallet.billing.AppcoinsBillingBinder.Companion.INAPP_PURCHASE_ID
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BillingMessagesMapper @Inject constructor() {

  companion object {
    internal const val TRANSACTION_HASH = "transaction_hash"
    internal const val TOP_UP_AMOUNT = "top_up_amount"
    internal const val TOP_UP_CURRENCY = "currency"
    internal const val TOP_UP_CURRENCY_SYMBOL = "currency_symbol"
    internal const val BONUS = "bonus"
    internal const val VALID_BONUS = "valid_bonus"
  }


  internal fun mapSupported(supportType: Billing.BillingSupportType): Int =
      when (supportType) {
        Billing.BillingSupportType.SUPPORTED -> AppcoinsBillingBinder.RESULT_OK
        Billing.BillingSupportType.MERCHANT_NOT_FOUND -> AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.UNKNOWN_ERROR -> AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.NO_INTERNET_CONNECTION -> AppcoinsBillingBinder.RESULT_SERVICE_UNAVAILABLE
        Billing.BillingSupportType.API_ERROR -> AppcoinsBillingBinder.RESULT_ERROR
      }


  private fun map(throwable: Throwable?): Int {
    return throwable?.let {
      when (it) {
        is BillingException -> it.getErrorCode()
        is IOException -> AppcoinsBillingBinder.RESULT_SERVICE_UNAVAILABLE
        is IllegalArgumentException -> AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR
        else -> AppcoinsBillingBinder.RESULT_ERROR
      }
    } ?: AppcoinsBillingBinder.RESULT_ERROR
  }

  fun mapSkuDetails(serializedProducts: List<String>): Bundle {
    val result = Bundle()
    result.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    result.putStringArrayList(AppcoinsBillingBinder.DETAILS_LIST, ArrayList(serializedProducts))
    return result
  }

  fun mapSkuDetailsError(exception: Exception): Bundle {
    val result = Bundle()
    result.putInt(AppcoinsBillingBinder.RESPONSE_CODE, map(exception.cause))
    return result
  }

  fun mapPurchasesError(exception: Exception): Bundle {
    val result = Bundle()
    result.putInt(AppcoinsBillingBinder.RESPONSE_CODE, map(exception.cause))
    return result
  }

  fun mapBuyIntentError(exception: Exception): Bundle {
    val result = Bundle()
    result.putInt(AppcoinsBillingBinder.RESPONSE_CODE, map(exception.cause))
    return result
  }

  fun mapConsumePurchasesError(exception: Exception): Int {
    return map(exception.cause)
  }

  fun mapCancellation(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_USER_CANCELED)
    return bundle
  }

  fun mapInvalidSubscriptionData(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_DEVELOPER_ERROR)
    return bundle
  }

  fun mapPurchase(purchaseId: String, signature: String, signatureData: String,
                  orderReference: String?): Bundle {
    val intent = Bundle()
    intent.putString(INAPP_PURCHASE_ID, purchaseId)
    intent.putString(INAPP_PURCHASE_DATA, signatureData)
    intent.putString(INAPP_DATA_SIGNATURE, signature)
    intent.putString(AppcoinsBillingBinder.INAPP_ORDER_REFERENCE, orderReference)
    intent.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    return intent
  }

  fun mapException(throwable: Throwable): Exception {
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is IOException -> ServiceUnavailableException(
          AppcoinsBillingBinder.RESULT_SERVICE_UNAVAILABLE)
      else -> UnknownException(AppcoinsBillingBinder.RESULT_ERROR)
    }
  }

  private fun mapHttpException(throwable: HttpException): Exception {
    return when (throwable.code()) {
      in 500..599 -> ApiException(
          AppcoinsBillingBinder.RESULT_ERROR)
      else -> ApiException(
          AppcoinsBillingBinder.RESULT_ERROR)
    }
  }

  fun mapPurchase(purchase: Purchase, orderReference: String?): Bundle {
    return mapPurchase(purchase.uid, purchase.signature.value, purchase.signature.message,
        orderReference)
  }

  fun genericError(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_ERROR)
    return bundle
  }

  fun successBundle(uid: String?): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)

    bundle.putString(TRANSACTION_HASH, uid)

    return bundle
  }

  fun topUpBundle(amount: String, currency: String, bonus: String,
                  fiatCurrencySymbol: String): Bundle {
    return Bundle().apply {
      putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
      putString(TOP_UP_AMOUNT, amount)
      putString(TOP_UP_CURRENCY, currency)
      putString(BONUS, bonus)
      putString(TOP_UP_CURRENCY_SYMBOL, fiatCurrencySymbol)
    }
  }

  fun mapFinishedPurchase(purchase: Purchase, itemAlreadyOwned: Boolean): Bundle {
    val bundle = Bundle()
    bundle.putString(INAPP_PURCHASE_DATA, purchase.signature.message)
    bundle.putString(INAPP_DATA_SIGNATURE, purchase.signature.value)
    bundle.putString(INAPP_PURCHASE_ID, purchase.uid)
    if (itemAlreadyOwned) {
      bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE,
          AppcoinsBillingBinder.RESULT_ITEM_ALREADY_OWNED)
    }
    return bundle
  }
}