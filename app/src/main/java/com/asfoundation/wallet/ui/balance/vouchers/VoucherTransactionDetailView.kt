package com.asfoundation.wallet.ui.balance.vouchers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.FrameLayout
import com.asf.wallet.R
import com.asfoundation.wallet.util.getStringSpanned
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_voucher_code_detail.view.*

class VoucherTransactionDetailView : FrameLayout {

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.layout_voucher_code_detail, this)
    setupUi()
  }

  private fun setupUi() {
    copy_code.setOnClickListener {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
      val clip = ClipData.newPlainText("voucher_code", code_text.text)
      clipboard?.setPrimaryClip(clip)
      Snackbar.make(this, context.getString(R.string.copy), Snackbar.LENGTH_LONG)
          .show()
    }
  }

  fun setCode(code: String) {
    code_text.text = code
  }

  fun setRedeemWebsite(text: String, url: String) {
    val spannableString = SpannableString(text)
    spannableString.setSpan(URLSpan(url), 0, spannableString.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    redeem_instructions.text =
        context?.getStringSpanned(R.string.voucher_instructions_body, spannableString)
    redeem_instructions.movementMethod = LinkMovementMethod.getInstance()
  }

}