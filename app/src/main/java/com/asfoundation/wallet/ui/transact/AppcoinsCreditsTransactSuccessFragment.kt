package com.asfoundation.wallet.ui.transact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment

class AppcoinsCreditsTransactSuccessFragment : DaggerFragment() {
  companion object {
    fun newInstance(): AppcoinsCreditsTransactSuccessFragment {
      return AppcoinsCreditsTransactSuccessFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.transaction_activity_layout, container, false)
  }
}
