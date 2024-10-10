package com.asfoundation.wallet.promo_code.bottom_sheet.success


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.rewards.RewardsAnalytics
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsPromoCodeSuccessBottomSheetLayoutBinding
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeBottomSheetNavigator
import com.asfoundation.wallet.wallet_reward.RewardSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromoCodeSuccessBottomSheetFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: PromoCodeBottomSheetNavigator

  private val views by viewBinding(SettingsPromoCodeSuccessBottomSheetLayoutBinding::bind)

  private val rewardSharedViewModel: RewardSharedViewModel by activityViewModels()

  @Inject
  lateinit var rewardsAnalytics: RewardsAnalytics

  companion object {

    private const val PROMO_CODE = "promo_code"

    @JvmStatic
    fun newInstance(promoCode: PromoCode): PromoCodeSuccessBottomSheetFragment {
      return PromoCodeSuccessBottomSheetFragment()
        .apply {
          arguments = Bundle().apply {
            putSerializable(PROMO_CODE, promoCode)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    rewardsAnalytics.promoCodeSuccessImpressionEvent(
      getSerializableExtra<PromoCode>(PROMO_CODE)?.code ?: ""
    )
    return SettingsPromoCodeSuccessBottomSheetLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    showSuccess(getSerializableExtra<PromoCode>(PROMO_CODE)!!)
    views.promoCodeBottomSheetSuccessGotItButton.setOnClickListener {
      rewardsAnalytics.promoCodeSuccessGotItClickEvent(
        getSerializableExtra<PromoCode>(PROMO_CODE)?.code ?: ""
      )
      rewardSharedViewModel.onBottomSheetDismissed()
      navigator.navigateBack()
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(views.root.parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeDraggable
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit

  @SuppressLint("StringFormatMatches")
  private fun showSuccess(promoCode: PromoCode) {
    views.promoCodeBottomSheetSuccessImage.visibility = View.VISIBLE
    if (promoCode.appName != null) {
      views.promoCodeBottomSheetSuccessSubtitle.text =
        this.getString(
          R.string.promo_code_success_body_specific_app,
          promoCode.bonus?.toInt().toString(),
          promoCode.appName
        )
    } else {
      views.promoCodeBottomSheetSuccessSubtitle.text =
        this.getString(
          R.string.promo_code_success_body,
          promoCode.bonus?.toInt().toString()
        )
    }
  }
}