package com.asfoundation.wallet.transactions

import androidx.annotation.StringRes
import com.asf.wallet.R

data class PaymentMethodDetails(
  val paymentKey: String,
  @StringRes val displayTextRes: Int? = null,
) {
  companion object {
    val paymentMethods: Map<String, PaymentMethodDetails> = mapOf(
      "credit_card" to PaymentMethodDetails(
        "credit_card",
        R.string.credit_debit_card
      ),
      "paypal" to PaymentMethodDetails(
        "paypal",
        R.string.paypal
      ),
      "paypal_v2" to PaymentMethodDetails(
        "paypal_v2",
        R.string.paypal
      ),
      "challenge_reward" to PaymentMethodDetails(
        "challenge_reward",
        R.string.challenge_reward_card_title
      ),
      "vk_pay" to PaymentMethodDetails(
        "vk_pay",
        R.string.mir_card
      ),
      "carrier_billing" to PaymentMethodDetails(
        "carrier_billing",
        R.string.carrier_billing_payment_method_title
      ),
      "sandbox" to PaymentMethodDetails(
        "sandbox",
        R.string.sandbox
      ),
      "googlepay" to PaymentMethodDetails(
        "googlepay",
        R.string.google_pay
      ),
      "mipay" to PaymentMethodDetails(
        "mipay",
        R.string.mipay
      ),
      "truelayer" to PaymentMethodDetails(
        "truelayer",
        R.string.bank_transfer
      ),
      "alipay_cn" to PaymentMethodDetails(
        "alipay_cn",
        R.string.alipay
      ),
      "credit_card_wallet_one" to PaymentMethodDetails(
        "credit_card_wallet_one",
        R.string.wallet_one
      ),
      "amazonpay" to PaymentMethodDetails(
        "amazonpay",
        R.string.amazon_pay
      ),
      "alfamart" to PaymentMethodDetails(
        "alfamart",
        R.string.alfamart
      ),
      "bank_transfer" to PaymentMethodDetails(
        "bank_transfer",
        R.string.bank_transfer
      ),
      "gopay" to PaymentMethodDetails(
        "gopay",
        R.string.gopay
      ),
      "ovo" to PaymentMethodDetails(
        "ovo",
        R.string.ovo
      ),
      "dana" to PaymentMethodDetails(
        "dana",
        R.string.dana
      ),
      "linkaja" to PaymentMethodDetails(
        "linkaja",
        R.string.linkaja
      ),
      "doku_wallet" to PaymentMethodDetails(
        "doku_wallet",
        R.string.doku_wallet
      ),
      "oxxo" to PaymentMethodDetails(
        "oxxo",
        R.string.oxxo
      ),
      "gcash" to PaymentMethodDetails(
        "gcash",
        R.string.gcash
      ),
      "true_money_wallet" to PaymentMethodDetails(
        "true_money_wallet",
        R.string.true_money_wallet
      ),
      "rabbit_line_pay" to PaymentMethodDetails(
        "rabbit_line_pay",
        R.string.rabbit_line_pay
      ),
      "ask_friend" to PaymentMethodDetails(
        "ask_friend",
        R.string.askafriend_payment_option_button
      ),
      "appcoins_credits" to PaymentMethodDetails(
        "appcoins_credits",
        R.string.appc
      ),
      "APPC Credits" to PaymentMethodDetails(
        "APPC Credits",
        R.string.appc
      ),
      "boleto" to PaymentMethodDetails(
        "boleto",
        R.string.boleto
      ),
      "paytm_upi" to PaymentMethodDetails(
        "paytm_upi",
        R.string.paytm_upi
      ),
      "paytm_wallet" to PaymentMethodDetails(
        "paytm_wallet",
        R.string.paytm_wallet
      ),
      "qiwi" to PaymentMethodDetails(
        "qiwi",
        R.string.qiwi
      ),
      "yoo_money" to PaymentMethodDetails(
        "yoo_money",
        R.string.yoo_money
      ),
    )

    fun getDetails(paymentKey: String): PaymentMethodDetails {
      return paymentMethods[paymentKey]
        ?: PaymentMethodDetails(paymentKey, null)
    }
  }
}