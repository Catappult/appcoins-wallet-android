package com.asfoundation.wallet.subscriptions

import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class SubscriptionApiMockedImpl : SubscriptionService {

  override fun getActiveSubscriptions(walletAddress: String): Single<List<Subscription>> {
    return Single.create<List<Subscription>> {
      val subs1 = Subscription("Real Boxing", "com.vividgames.realboxing",
          "http://pool.img.aptoide.com/bds-store/59a7b62a169a832e96dbd7df82d6e3cc_icon.png",
          BigDecimal.TEN, "€", "EUR", true, "PayPal",
          PAYPAL_ICON_URL, getFutureDate(), getPastDate(), getPastDate(), null, "Month")
      val subs2 = Subscription("Cuties", "com.celticspear.blackies",
          "http://pool.img.aptoide.com/bds-store/d6267357ec641dd583a0ad318fa0741b_icon.png",
          BigDecimal("3.56"), "€", "EUR", true, "Credit Card",
          CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), getFutureDate(), "Month")
      it.onSuccess(listOf(subs1, subs2))
    }
  }

  override fun getExpiredSubscriptions(walletAddress: String): Single<List<Subscription>> {
    return Single.create<List<Subscription>> {
      val subs1 = Subscription("Creative Destruction", "com.titan.cd.gb",
          "http://pool.img.aptoide.com/bds-store/fc1a8567262637b89f4f8bd9f0c69559_icon.jpg",
          BigDecimal.ONE, "€", "EUR", false, "Credit Card",
          CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), null, "Week")
      it.onSuccess(listOf(subs1))
    }
  }

  override fun getSubscriptionDetails(packageName: String, address: String): Single<Subscription> {
    return Single.create<Subscription> {
      when (packageName) {
        "com.vividgames.realboxing" -> {
          val subs1 = Subscription("Real Boxing", "com.vividgames.realboxing",
              "http://pool.img.aptoide.com/bds-store/59a7b62a169a832e96dbd7df82d6e3cc_icon.png",
              BigDecimal.TEN, "€", "EUR", true, "PayPal",
              PAYPAL_ICON_URL, getFutureDate(), getPastDate(), getPastDate(), null, "Month")
          it.onSuccess(subs1)
        }
        "com.titan.cd.gb" -> {
          val subs1 = Subscription("Creative Destruction", "com.titan.cd.gb",
              "http://pool.img.aptoide.com/bds-store/fc1a8567262637b89f4f8bd9f0c69559_icon.jpg",
              BigDecimal.ONE, "€", "EUR", false, "Credit Card",
              CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), null, "Week")
          it.onSuccess(subs1)
        }
        "com.celticspear.blackies" -> {
          val subs1 = Subscription("Cuties", "com.celticspear.blackies",
              "http://pool.img.aptoide.com/bds-store/d6267357ec641dd583a0ad318fa0741b_icon.png",
              BigDecimal("3.56"), "€", "EUR", true, "Credit Card",
              CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), getFutureDate(), "Week")
          it.onSuccess(subs1)
        }
        else -> {
          it.onError(Throwable("Subscription not found"))
        }
      }
    }
  }

  override fun getSubscriptionByTransactionId(transactionId: String): Single<Subscription> {
    return Single.create<Subscription> {
      when (transactionId) {
        "0xd6d42df92b55be4b7d24c96c3dc546474ad638ff66cb061f2fd05e9b74e4e6a1" -> {
          val subs1 = Subscription("Real Boxing", "com.vividgames.realboxing",
              "http://pool.img.aptoide.com/bds-store/59a7b62a169a832e96dbd7df82d6e3cc_icon.png",
              BigDecimal.TEN, "€", "EUR", true, "PayPal",
              PAYPAL_ICON_URL, getFutureDate(), getPastDate(), getPastDate(), null, "Month")
          it.onSuccess(subs1)
        }
        "0xa03f872318ee763e7cd92923304671e0115f883c32c0520ca3b7c3a1a9d47f98" -> {
          val subs1 = Subscription("Creative Destruction", "com.titan.cd.gb",
              "http://pool.img.aptoide.com/bds-store/fc1a8567262637b89f4f8bd9f0c69559_icon.jpg",
              BigDecimal.ONE, "€", "EUR", false, "Credit Card",
              CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), null, "Week")
          it.onSuccess(subs1)
        }
        "0xca74e82bc850c7dc5afad05387ba314de579b8552269200821e6c39d285e4ff9-2" -> {
          val subs1 = Subscription("Cuties", "com.celticspear.blackies",
              "http://pool.img.aptoide.com/bds-store/d6267357ec641dd583a0ad318fa0741b_icon.png",
              BigDecimal("3.56"), "€", "EUR", true, "Credit Card",
              CREDIT_CARD_ICON_URL, null, getPastDate(), getPastDate(), getFutureDate(), "Week")
          it.onSuccess(subs1)
        }
        else -> {
          it.onError(Throwable("Subscription not found"))
        }
      }
    }
  }


  private fun getFutureDate(): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, Random().nextInt(5))
    return cal.time
  }

  private fun getPastDate(): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, Random().nextInt(5) * -1)
    return cal.time
  }


  companion object {

    const val PAYPAL_ICON_URL =
        "https://cdn6.aptoide.com/imgs/0/2/b/02b57118b06b81958ab1baf4788ce09d_logo.png"
    const val CREDIT_CARD_ICON_URL =
        "https://cdn6.aptoide.com/imgs/b/4/b/b4bd2e30853b976ec80544b7f8c0a0d7_logo.png"

  }

}
