package com.asfoundation.wallet.promotions.ui.vip_referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVipReferralBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromotionsVipReferralFragment: BasePageViewFragment() ,
  SingleStateFragment<PromotionsVipReferralState, PromotionsVipReferralSideEffect> {

  @Inject
  lateinit var navigator: PromotionsVipReferralNavigator

  private val viewModel: PromotionsVipReferralViewModel by viewModels()

  private var binding: FragmentVipReferralBinding? = null
  private val views get() = binding!!

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  lateinit var promoReferral: String
  lateinit var earnedValue: String
  lateinit var earnedTotal: String

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentVipReferralBinding.inflate(inflater, container, false)
    return views.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setListeners()
    viewModel.getCurrency(earnedValue)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setupView() = requireArguments().run {
    val bonusPercent = getString(BONUS_PERCENT) ?: ""
    promoReferral = getString(PROMO_REFERRAL) ?: ""
    earnedValue = getString(EARNED_VALUE) ?: ""
    earnedTotal = getString(EARNED_TOTAL) ?: ""
    views.descriptionTv.text = context?.getString(R.string.vip_program_referral_page_body, bonusPercent)
    views.codeTv?.text = promoReferral
    views.earnedTv?.text = context?.getString(R.string.vip_program_referral_page_earned_body, "$", earnedValue, earnedTotal)
  }

  override fun onStateChanged(state: PromotionsVipReferralState) {
    when (state.convertTotalAsync) {
      is Async.Uninitialized -> {

      }
      is Async.Loading -> {

      }
      is Async.Fail -> {
        views.earnedTv?.text = context?.getString(
          R.string.vip_program_referral_page_earned_body,
          "",
          earnedValue,
          earnedTotal
        )
      }
      is Async.Success -> {
        state.convertTotalAsync.value?.let { convertedTotal ->
          val fiatAmount =
            formatter.formatCurrency(convertedTotal.amount, WalletCurrency.FIAT)
          views.earnedTv?.text = context?.getString(
            R.string.vip_program_referral_page_earned_body,
            convertedTotal.symbol ?: "",
            fiatAmount,
            earnedTotal
          )
        }
      }
    }
  }

  private fun setListeners() {
    views.topBar?.barBackButton?.setOnClickListener { navigator.navigateBack() }
    views.shareBt?.setOnClickListener { shareCode() }
    views.codeTv?.setOnClickListener { copyCode() }
  }

  private fun shareCode() {
    ShareCompat.IntentBuilder(requireActivity())
      .setText(promoReferral)
      .setType("text/*")
      .setChooserTitle(resources.getString(R.string.share_via))
      .startChooser()
  }

  private fun copyCode() {
    val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("Referral Code", promoReferral)
    clipboard?.setPrimaryClip(clip)
    Snackbar.make(views.window as View, R.string.copied, Snackbar.LENGTH_SHORT)
      .show()
  }

  override fun onSideEffect(sideEffect: PromotionsVipReferralSideEffect) = Unit

  companion object {
    internal const val BONUS_PERCENT = "vip_bonus"
    internal const val PROMO_REFERRAL = "vip_code"
    internal const val EARNED_VALUE = "total_earned"
    internal const val EARNED_TOTAL = "number_referrals"
  }

}
