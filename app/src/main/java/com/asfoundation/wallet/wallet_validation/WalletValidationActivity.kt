package com.asfoundation.wallet.wallet_validation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.iab.IabActivity.RESPONSE_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.RESULT_USER_CANCELED
import dagger.android.AndroidInjection

class WalletValidationActivity : BaseActivity(), WalletValidationActivityView {

  private lateinit var presenter: WalletValidationPresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WalletValidationActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_validation)
    presenter = WalletValidationPresenter(this)
    presenter.present()
  }

  override fun finish() {
    super.finish()
  }

  override fun showError() {
    setResult(Activity.RESULT_CANCELED)
    finish()
  }

  override fun onBackPressed() {
    val bundle = Bundle()
    bundle.putInt(RESPONSE_CODE, RESULT_USER_CANCELED)
    close(bundle)
    super.onBackPressed()
  }

  override fun close(bundle: Bundle?) {
    val intent = Intent()
    if (bundle != null) {
      intent.putExtras(bundle)
    }
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PhoneValidationFragment.newInstance(countryCode, phoneNumber))
        .commit()
  }

  override fun showCodeValidationView(countryCode: String, phoneNumber: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(countryCode, phoneNumber))
        .commit()
  }

  override fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(validationInfo, errorMessage))
        .commit()
  }

  override fun showLoading(it: ValidationInfo) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            ValidationLoadingFragment.newInstance(it))
        .commit()
  }

  override fun showSuccess() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            ValidationSuccessFragment.newInstance())
        .commit()
  }

}