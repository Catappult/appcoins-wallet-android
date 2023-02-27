package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

//type=TOPUP&status=COMPLETED&wallet_from=0xcA8c45737325B04DE6A2b986EbffEEfB51D0FA10
/** Copied from com.appcoins.wallet.bdsbilling.repository.entity*/
class Gateway(val name: Name?, val label: String, val icon: String) {

  companion object {
    fun unknown(): Gateway {
      return Gateway(Name.unknown, "unknown", "unknown")
    }
  }

  enum class Name {
    appcoins, adyen_v2, unknown, appcoins_credits, myappcoins
  }
}

data class Transaction(
  val uid: String,
  val status: Status,
  val gateway: Gateway?,
  var hash: String?,
  val metadata: Metadata?,
  val orderReference: String?,
  val price: TransactionPrice?,
  val type: String,
  val wallets: WalletsResponse?,
  val url: String? = null
) {

  companion object {
    fun notFound(): Transaction {
      return Transaction(
        "", Status.INVALID_TRANSACTION, Gateway.unknown(), null, null, null, null,
        "", null, null
      )
    }

  }

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, PROCESSING, COMPLETED, PENDING_USER_PAYMENT,
    INVALID_TRANSACTION, FAILED, CANCELED, FRAUD, SETTLED
  }

}

data class Metadata(@SerializedName("purchase_uid") val purchaseUid: String)

data class WalletsResponse(
  val developer: String?, val store: String?, val oem: String?,
  val user: String?
)

data class TransactionPrice(val currency: String, val value: String, val appc: String)

data class TopUpResponse(
  val items: List<Transaction>? = null
)