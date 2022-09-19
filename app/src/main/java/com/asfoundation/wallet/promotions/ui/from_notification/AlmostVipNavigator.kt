package com.asfoundation.wallet.promotions.ui.from_notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.AlmostVipFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.promotions.ui.PromotionsSideEffect
import com.asfoundation.wallet.promotions.ui.PromotionsState
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlmostVipNavigator : BasePageViewFragment(),
  SingleStateFragment<PromotionsState, PromotionsSideEffect> {

  private val views by viewBinding(AlmostVipFragmentBinding::bind)


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.almost_vip_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.almostVipGotItButton.setOnClickListener {

    }
  }

  override fun onStateChanged(state: PromotionsState) = Unit

  override fun onSideEffect(sideEffect: PromotionsSideEffect) = Unit
}