package com.asfoundation.wallet.withdraw.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R

class WithdrawFragment : Fragment() {
  companion object {
    fun newInstance(): WithdrawFragment {
      val fragment = WithdrawFragment()

      return fragment
    }
  }


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_withdraw, container, false)
  }



}
