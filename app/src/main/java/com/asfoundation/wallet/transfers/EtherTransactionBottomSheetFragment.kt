package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.EtherTransactionBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EtherTransactionBottomSheetFragment : BottomSheetDialogFragment(),
    EtherTransactionBottomSheetView {

  @Inject
  lateinit var presenter: EtherTransactionBottomSheetPresenter

  private val binding by viewBinding(EtherTransactionBottomSheetBinding::bind)

  init {
    isCancelable = false
  }

  companion object {

    const val TRANSACTION_KEY = "transaction_hash"

    private const val TRANSACTION_HASH_CLIPBOARD = "eth_transaction_hash_clipboard"

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
    return R.style.AppBottomSheetDialogThemeNotDraggable
  }

  override fun setTransactionHash(transactionHash: String) {
    binding.etherTransactionBottomSheetHashString.text = transactionHash
  }

  override fun getEtherScanClick() = RxView.clicks(binding.etherTransactionBottomSheetRectangle)

  override fun getClipboardClick() = RxView.clicks(binding.etherTransactionBottomSheetCopyClipboard)

  override fun copyToClipboard(transactionHash: String) {
    val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(TRANSACTION_HASH_CLIPBOARD, transactionHash)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(activity, R.string.copied, Toast.LENGTH_SHORT)
        .show()
  }

  override fun getOkClick() = RxView.clicks(binding.etherTransactionBottomSheetGotItButton)

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}