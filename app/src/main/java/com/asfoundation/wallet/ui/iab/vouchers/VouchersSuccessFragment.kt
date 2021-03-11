package com.asfoundation.wallet.ui.iab.vouchers

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_vouchers_success.*
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

class VouchersSuccessFragment : DaggerFragment(), VouchersSuccessView {

  @Inject
  lateinit var presenter: VouchersSuccessPresenter

  private lateinit var iabView: IabView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Adyen payment fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {

    return inflater.inflate(R.layout.fragment_vouchers_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    iabView.unlockRotation()
    presenter.present()
  }

  override fun setupUi(bonus: String, code: String, redeem: String) {
    handleBonusAnimation(bonus)
    voucher_details.setCode(code)
    val redeemText = redeem.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "")
    voucher_details.setRedeemWebsite(redeemText, redeem)
  }

  override fun getGotItClick(): Observable<Any> = RxView.clicks(vouchers_got_it_button)

  private fun handleBonusAnimation(bonus: String) {
    if (StringUtils.isNotBlank(bonus)) {
      vouchers_lottie_transaction_success.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation(bonus)
    } else {
      vouchers_lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
    vouchers_lottie_transaction_success.playAnimation()
  }

  private fun setupTransactionCompleteAnimation(bonus: String) {
    val textDelegate = TextDelegate(vouchers_lottie_transaction_success)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText("bonus_received",
        resources.getString(R.string.gamification_purchase_completed_bonus_received))
    vouchers_lottie_transaction_success.setTextDelegate(textDelegate)
    vouchers_lottie_transaction_success.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    const val BONUS_KEY = "bonus_key"
    const val CODE_KEY = "code"
    const val REDEEM_LINK_KEY = "redeem_link"

    @JvmStatic
    fun newInstance(code: String, redeemLink: String, bonus: String = ""): VouchersSuccessFragment {
      val fragment = VouchersSuccessFragment()
      fragment.arguments = Bundle().apply {
        putString(BONUS_KEY, bonus)
        putString(CODE_KEY, code)
        putString(REDEEM_LINK_KEY, redeemLink)
      }
      return fragment
    }
  }
}