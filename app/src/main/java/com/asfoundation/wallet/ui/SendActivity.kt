package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.router.Result
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.iab.IabActivity
import com.appcoins.wallet.core.utils.android_common.Log.Companion.e
import com.asfoundation.wallet.viewmodel.SendViewModel
import com.asfoundation.wallet.viewmodel.SendViewModelFactory
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.NumberFormat
import javax.inject.Inject

@AndroidEntryPoint
class SendActivity : BaseActivity() {
  @Inject
  lateinit var sendViewModelFactory: SendViewModelFactory
  lateinit var viewModel: SendViewModel
  lateinit var toAddressText: EditText
  lateinit var amountText: EditText
  lateinit var toInputLayout: TextInputLayout
  lateinit var amountInputLayout: TextInputLayout

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send)
    toolbar()
    toInputLayout = findViewById(R.id.to_input_layout)
    toAddressText = findViewById(R.id.send_to_address)
    amountInputLayout = findViewById(R.id.amount_input_layout)
    amountText = findViewById(R.id.send_amount)
    viewModel = ViewModelProvider(this, sendViewModelFactory)
      .get(SendViewModel::class.java)
    viewModel.init(
      intent.getParcelableExtra(C.EXTRA_TRANSACTION_BUILDER),
      intent.data
    )
    viewModel.symbol()
      .observe(this) { symbol: String? -> onSymbol(symbol) }
    viewModel.amount()
      .observe(this) { bigDecimal: BigDecimal -> onAmount(bigDecimal) }
    viewModel.toAddress()
      .observe(this) { toAddress: String -> onToAddress(toAddress) }
    viewModel.onTransactionSucceed()
      .observe(this) { result: Result ->
        onFinishWithResult(
          result
        )
      }
    val scanBarcodeButton = findViewById<ImageButton>(R.id.scan_barcode_button)
    scanBarcodeButton.setOnClickListener { view: View? ->
      val intent = Intent(
        applicationContext,
        BarcodeCaptureActivity::class.java
      )
      startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_next -> {
        onNext()
      }
      android.R.id.home -> {
        onBackPressed()
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (!viewModel.onActivityResult(requestCode, resultCode, data)
      && requestCode == BARCODE_READER_REQUEST_CODE
    ) {
      if (resultCode == CommonStatusCodes.SUCCESS) {
        if (data != null) {
          val barcode: Barcode? = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject)
          if (!viewModel.extractFromQR(barcode)) {
            Toast.makeText(this, R.string.toast_qr_code_no_address, Toast.LENGTH_SHORT)
              .show()
          }
        }
      } else {
        e(
          "SEND", String.format(
            getString(R.string.barcode_error_format),
            CommonStatusCodes.getStatusCodeString(resultCode)
          )
        )
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun onFinishWithResult(result: Result) {
    if (result.isSuccess) {
      setResult(RESULT_OK, result.data)
      finish()
    }
  }

  private fun onAmount(bigDecimal: BigDecimal) {
    amountText!!.setText(
      NumberFormat.getInstance()
        .format(bigDecimal)
    )
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.send_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  private fun onNext() {
    // Validate input fields
    var hasError = false
    val to = toAddressText!!.text
      .toString()
    val amount = amountText!!.text
      .toString()
    if (!viewModel.setToAddress(to)) {
      toInputLayout!!.error = getString(R.string.error_invalid_address)
      hasError = true
    }
    if (!viewModel.setAmount(amount)) {
      amountInputLayout!!.error = getString(R.string.error_invalid_amount)
      hasError = true
    }
    if (!hasError) {
      toInputLayout!!.isErrorEnabled = false
      amountInputLayout!!.isErrorEnabled = false
      viewModel.openConfirmation(this)
    }
  }

  private fun onToAddress(toAddress: String) {
    // Populate to address if it has been passed forward
    toAddressText!!.setText(toAddress)
  }

  private fun onSymbol(symbol: String?) {
    if (symbol != null) {
      setTitle(String.format(getString(R.string.title_send_with_token), symbol))
      amountInputLayout!!.hint =
        String.format(getString(R.string.hint_amount_with_token), symbol)
    }
  }

  companion object {
    private const val BARCODE_READER_REQUEST_CODE = 1
    fun newIntent(context: Context?, previousIntent: Intent): Intent {
      val intent = Intent(context, IabActivity::class.java)
      intent.data = previousIntent.data
      if (previousIntent.extras != null) {
        intent.putExtras(previousIntent.extras!!)
      }
      return intent
    }
  }
}