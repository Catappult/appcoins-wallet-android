package com.asfoundation.wallet.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.databinding.GenericLoadingBinding

class LoadingFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = GenericLoadingBinding.inflate(inflater).root
}