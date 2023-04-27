package com.asfoundation.wallet.promotions.ui.from_notification

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.AlmostVipFragmentBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.promotions.ui.PromotionsSideEffect
import com.asfoundation.wallet.promotions.ui.PromotionsState
import com.asfoundation.wallet.viewmodel.BasePageViewDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlmostVipDialogFragment : BasePageViewDialogFragment(),
  SingleStateFragment<PromotionsState, PromotionsSideEffect> {

  private val views by viewBinding(AlmostVipFragmentBinding::bind)

  companion object {
    var showing = false
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    object : Dialog(requireContext(), theme) {
      override fun onBackPressed() {
        // Do nothing
      }
    }

  override fun getTheme(): Int = R.style.FullScreenDialogStyle

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    if (showing) dismiss() else showing = true
    return AlmostVipFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.almostVipGotItButton.setOnClickListener {
      dismiss()
      showing = false
    }
  }

  override fun onStateChanged(state: PromotionsState) = Unit

  override fun onSideEffect(sideEffect: PromotionsSideEffect) = Unit
}