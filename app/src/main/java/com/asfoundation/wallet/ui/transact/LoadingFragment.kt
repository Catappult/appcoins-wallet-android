package com.asfoundation.wallet.ui.transact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R

class LoadingFragment : Fragment() {
  companion object {
    fun newInstance(): Fragment {
      return LoadingFragment()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.transact_loading_view, container, false)
  }

}
