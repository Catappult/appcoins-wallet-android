package com.asfoundation.wallet.promotions.ui.from_notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.AlmostVipFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.promotions.ui.PromotionsSideEffect
import com.asfoundation.wallet.promotions.ui.PromotionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlmostVipFragment : DialogFragment(),
  SingleStateFragment<PromotionsState, PromotionsSideEffect> {

  private val views by viewBinding(AlmostVipFragmentBinding::bind)

  override fun getTheme(): Int = R.style.FullScreenDialogStyle

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.almost_vip_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    startPostponedEnterTransition()
    views.almostVipGotItButton.setOnClickListener {
      this.dismiss()
    }
  }

  override fun onStateChanged(state: PromotionsState) = Unit

  override fun onSideEffect(sideEffect: PromotionsSideEffect) = Unit
}