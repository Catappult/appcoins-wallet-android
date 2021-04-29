package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.ether_transaction_bottom_sheet.*
import javax.inject.Inject

class EtherTransactionBottomSheetFragment : DaggerBottomSheetDialogFragment(),
    EtherTransactionBottomSheetView {

  @Inject
  lateinit var presenter: EtherTransactionBottomSheetPresenter

  private val transactionHash: String by lazy {
    if (arguments!!.containsKey(
            HASH_KEY)) {
      arguments!!.getString(
          HASH_KEY, null)
    } else {
      throw IllegalArgumentException("Transaction Hash not found")
    }
  }

  companion object {

    const val HASH_KEY = "hash_key"

    @JvmStatic
    fun newInstance(transactionHash: String): EtherTransactionBottomSheetFragment {
      return EtherTransactionBottomSheetFragment()
          .apply {
            arguments = Bundle().apply {
              putSerializable(
                  HASH_KEY, transactionHash)
            }
          }
    }
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

  fun setAnimations() {
    dialog?.window
        ?.attributes?.windowAnimations = R.anim.fragment_slide_up;
    ether_transaction_bottom_sheet_layout.animation =
        AnimationUtils.loadAnimation(context, R.anim.fragment_slide_up)
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogTheme
  }

  override fun setTransactionHash() {
    ether_transaction_bottom_sheet_hash_string.text = transactionHash
  }

  override fun getEtherScanClick() = RxView.clicks(ether_transaction_bottom_sheet_rectangle)

  override fun getClipboardClick() = RxView.clicks(ether_transaction_bottom_sheet_copy_clipboard)

  override fun copyToClipboard() {
    val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("transaction transaction", transactionHash)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(activity, "Copied to clipboard", Toast.LENGTH_SHORT)
        .show()
  }

  override fun getOkClick() = RxView.clicks(ether_transaction_bottom_sheet_got_it_button)

  override fun onDestroyView() {
    super.onDestroyView()
    ether_transaction_bottom_sheet_layout.animation =
        AnimationUtils.loadAnimation(context, R.anim.fragment_slide_down)
    presenter.stop()
  }
}