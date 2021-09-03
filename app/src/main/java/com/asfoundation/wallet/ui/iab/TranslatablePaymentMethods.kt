package com.asfoundation.wallet.ui.iab

import com.asf.wallet.R

enum class TranslatablePaymentMethods(val paymentMethod: String, val stringId: Int) {
  ASK_SOMEONE_PAY("ask_friend", R.string.askafriend_payment_option_button),
  CREDIT_CARD("credit_card", R.string.dialog_bank_card),
  EARN_APPCOINS("earn_appcoins", R.string.purchase_poa_item)
}