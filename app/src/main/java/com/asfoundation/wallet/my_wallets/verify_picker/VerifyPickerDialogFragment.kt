package com.asfoundation.wallet.my_wallets.verify_picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVerifyPickerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerifyPickerDialogFragment : BottomSheetDialogFragment() {

  @Inject
  lateinit var navigator: VerifyPickerDialogNavigator

  private val views by viewBinding(FragmentVerifyPickerBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = FragmentVerifyPickerBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.verifyWithPaypalCardView.setOnClickListener { navigator.navigateToPaypalVerify() }
    views.verifyWithCreditCardView.setOnClickListener { navigator.navigateToCreditCardVerify() }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable
}