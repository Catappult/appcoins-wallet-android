package com.asfoundation.wallet.my_wallets.neww

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentMyWalletsBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.qr_code_layout.*
import javax.inject.Inject

class NewMyWalletsFragment : BasePageViewFragment(),
    SingleStateFragment<MyWalletsState, MyWalletsSideEffect> {

  @Inject
  lateinit var viewModelFactory: MyWalletsViewModelFactory

  private val viewModel: MyWalletsViewModel by viewModels { viewModelFactory }

  private val views by viewBinding(FragmentMyWalletsBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_my_wallets, container, false)
  }

  override fun onStateChanged(state: MyWalletsState) {

  }

  override fun onSideEffect(sideEffect: MyWalletsSideEffect) {

  }

  fun createQrCode(walletAddress: String) {
    try {
      val logo = ResourcesCompat.getDrawable(resources, R.drawable.ic_appc_token, null)
      val mergedQrCode = walletAddress.generateQrCode(requireActivity().windowManager, logo!!)
      views.qrImage.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(main_layout, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
  }
}