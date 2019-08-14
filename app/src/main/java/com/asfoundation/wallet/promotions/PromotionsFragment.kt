package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment

class PromotionsFragment : DaggerFragment(), PromotionsView {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.promotions_fragment_view, container, false)
  }
}
