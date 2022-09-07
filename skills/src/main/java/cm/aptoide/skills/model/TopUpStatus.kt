package cm.aptoide.skills.model

/** Class copied from com.appcoins.wallet.billing.common.response */
enum class TopUpStatus {
  PENDING, PENDING_SERVICE_AUTHORIZATION, SETTLED, PROCESSING, COMPLETED, PENDING_USER_PAYMENT,
  INVALID_TRANSACTION, FAILED, CANCELED, FRAUD, PENDING_VALIDATION, PENDING_CODE, VERIFIED, EXPIRED
}