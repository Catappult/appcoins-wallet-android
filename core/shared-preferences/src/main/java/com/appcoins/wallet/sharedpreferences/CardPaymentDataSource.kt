package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class CardPaymentDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  fun isMandatoryCvc(): Boolean {
    return sharedPreferences.getBoolean(MANDATORY_CVC, false)
  }

  fun setMandatoryCvc(isMandatory: Boolean) {
    sharedPreferences.edit()
      .putBoolean(MANDATORY_CVC, isMandatory)
      .apply()
  }

  fun setGotItManageCard(isVisible: Boolean) {
    sharedPreferences.edit()
      .putBoolean(GOT_IT_MANAGE_CARD, isVisible)
      .apply()
  }

  fun setPreferredCardId(cardId: String) {
    sharedPreferences.edit()
      .putString(PREFERRED_CARD_ID, cardId)
      .apply()
  }


  fun getPreferredCardId(): String? {
    return sharedPreferences.getString(PREFERRED_CARD_ID, "")
  }


  fun isGotItVisible(): Boolean {
    return sharedPreferences.getBoolean(GOT_IT_MANAGE_CARD, true)
  }

  companion object {
    private const val MANDATORY_CVC = "mandatory_cvc"
    private const val GOT_IT_MANAGE_CARD = "got_it_manage_card"
    private const val PREFERRED_CARD_ID = "preferred_card_id"
  }
}
