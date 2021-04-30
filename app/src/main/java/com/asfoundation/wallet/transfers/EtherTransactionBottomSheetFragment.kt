package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.asf.wallet.R
import com.asfoundation.wallet.di.DaggerBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.ether_transaction_bottom_sheet.*
import javax.inject.Inject

class EtherTransactionBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    EtherTransactionBottomSheetView {

  @Inject
  lateinit var presenter: EtherTransactionBottomSheetPresenter

  companion object {

    const val TRANSACTION_KEY = "transaction_hash"

    const val transaction = "transaction"

    @JvmStatic
    fun newInstance(transactionHash: String): EtherTransactionBottomSheetFragment {
      return EtherTransactionBottomSheetFragment()
          .apply {
            arguments = Bundle().apply {
              putString(
                  TRANSACTION_KEY, transactionHash)
            }
          }
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.ether_transaction_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dialog?.setCanceledOnTouchOutside(false)
    presenter.present()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogTheme
  }

  override fun setTransactionHash(transactionHash: String) {
    ether_transaction_bottom_sheet_hash_string.text = transactionHash
  }

  override fun getEtherScanClick() = RxView.clicks(ether_transaction_bottom_sheet_rectangle)

  override fun getClipboardClick() = RxView.clicks(ether_transaction_bottom_sheet_copy_clipboard)

  override fun copyToClipboard(transactionHash: String) {
    val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(transaction, transactionHash)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT)
        .show()
  }

  override fun getOkClick() = RxView.clicks(ether_transaction_bottom_sheet_got_it_button)

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}