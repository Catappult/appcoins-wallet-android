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

  private var _binding: EtherTransactionBottomSheetBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val ether_transaction_bottom_sheet_hash_string get() = binding.etherTransactionBottomSheetHashString
  private val ether_transaction_bottom_sheet_rectangle get() = binding.etherTransactionBottomSheetRectangle
  private val ether_transaction_bottom_sheet_copy_clipboard get() = binding.etherTransactionBottomSheetCopyClipboard
  private val ether_transaction_bottom_sheet_got_it_button get() = binding.etherTransactionBottomSheetGotItButton

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
    _binding = EtherTransactionBottomSheetBinding.inflate(inflater, container, false)
    return binding.root
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
    ether_transaction_bottom_sheet_hash_string.text = transactionHash
  }

  override fun getEtherScanClick() = RxView.clicks(ether_transaction_bottom_sheet_rectangle)

  override fun getClipboardClick() = RxView.clicks(ether_transaction_bottom_sheet_copy_clipboard)

  override fun copyToClipboard(transactionHash: String) {
    val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(TRANSACTION_HASH_CLIPBOARD, transactionHash)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(activity, R.string.copied, Toast.LENGTH_SHORT)
        .show()
  }

  override fun getOkClick() = RxView.clicks(ether_transaction_bottom_sheet_got_it_button)

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
    _binding = null
  }
}