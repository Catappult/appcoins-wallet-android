package com.asfoundation.wallet.ui.balance.evouchers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Toast
import com.asf.wallet.R
import com.asfoundation.wallet.util.getStringSpanned
import kotlinx.android.synthetic.main.layout_voucher_code_detail.view.*

class VoucherDetailView : FrameLayout {

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.layout_voucher_code_detail, this)
    setupUi()
  }

  private fun setupUi() {
    copy_address.setOnClickListener {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
      val clip = ClipData.newPlainText("voucher_code", code_text.text)
      clipboard?.setPrimaryClip(clip)
      Toast.makeText(context, context.getString(R.string.copy), Toast.LENGTH_SHORT)
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