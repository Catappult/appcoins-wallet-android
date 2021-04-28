package com.asfoundation.wallet.transfers

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection

open class DaggerBottomSheetDialogFragment : BottomSheetDialogFragment() {

  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }
}