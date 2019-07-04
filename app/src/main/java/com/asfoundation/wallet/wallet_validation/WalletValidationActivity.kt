package com.asfoundation.wallet.wallet_validation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection

class WalletValidationActivity : BaseActivity(), WalletValidationView {

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

  override fun onBackPressed() {
    close()
    super.onBackPressed()
  }

  override fun close() {
    finish()
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?,
                                       errorMessage: Int?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PhoneValidationFragment.newInstance(countryCode, phoneNumber, errorMessage))
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