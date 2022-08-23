package com.asfoundation.wallet.promotions.ui.vip_referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.getSystemService
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVipReferralBinding
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.topup_bar_layout.*

@AndroidEntryPoint
class PromotionsVipReferralFragment: BasePageViewFragment() {

  private var binding: FragmentVipReferralBinding? = null
  private val views get() = binding!!

  lateinit var promoReferral: String

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
  }

  private fun setupView() = requireArguments().run {
    val bonusPercent = getString(BONUS_PERCENT) ?: "5"    //TODO
    promoReferral = getString(PROMO_REFERRAL) ?: "123456789"  //TODO
    val earnedValue = getString(EARNED_VALUE) ?: "100"//TODO
    val earnedTotal = getString(EARNED_TOTAL) ?: "10"//TODO
    views.descriptionTv.text = context?.getString(R.string.vip_program_referral_page_body, bonusPercent)
    views.codeTv?.text = promoReferral
    //TODO erro na string
    //views.earnedTv?.text = context?.getString(R.string.vip_program_referral_page_earned_body, earnedValue, earnedTotal)
  }

  private fun setListeners() {
    views.topBar?.barBackButton?.setOnClickListener { requireActivity().onBackPressed() }
    views.shareBt?.setOnClickListener { shareCode() }
    views.codeTv?.setOnClickListener { copyCode() }
  }

  private fun shareCode() {
    ShareCompat.IntentBuilder(requireActivity())
      .setText(promoReferral)
      .setType("text/plain")
      .setChooserTitle(resources.getString(R.string.share_via))
      .startChooser()
  }

  private fun copyCode() {
    val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("Referral Code", promoReferral)
    clipboard?.setPrimaryClip(clip)
    Snackbar.make(views.bottomEarnedCl as View, R.string.copied, Snackbar.LENGTH_SHORT)
      .apply { anchorView = views.bottomEarnedCl as View }
      .show()
  }

  companion object {
    internal const val BONUS_PERCENT = "bonus_percent"
    internal const val PROMO_REFERRAL = "promo_referral"
    internal const val EARNED_VALUE = "earned_value"
    internal const val EARNED_TOTAL = "earned_total"
  }

}