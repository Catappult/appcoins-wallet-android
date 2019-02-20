package com.asfoundation.wallet.ui.transact

import dagger.android.support.DaggerFragment

class TransactFragment : DaggerFragment(), TransactFragmentView {
  companion object {
    fun newInstance(): TransactFragment {
      return TransactFragment()
    }
  }

}
