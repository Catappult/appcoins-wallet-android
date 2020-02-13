package com.asfoundation.wallet.ui.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_backup_creation_layout.*

class BackupCreationFragment : DaggerFragment() {

  private lateinit var fragmentContainer: ViewGroup

  companion object {
    private const val PARAM_WALLET_ADDR = "PARAM_WALLET_ADDR"

    @JvmStatic
    fun newInstance(walletAddress: String): BackupCreationFragment {
      val bundle = Bundle()
      bundle.putString(PARAM_WALLET_ADDR, walletAddress)
      val fragment = BackupCreationFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    fragmentContainer = container!!
    return inflater.inflate(R.layout.fragment_backup_creation_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    backup_creation_animation.playAnimation()
  }
}
