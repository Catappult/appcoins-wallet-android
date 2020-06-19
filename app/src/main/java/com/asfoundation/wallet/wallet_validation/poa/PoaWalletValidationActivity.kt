package com.asfoundation.wallet.wallet_validation.poa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.R
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_iab_wallet_creation.*
import javax.inject.Inject

class PoaWalletValidationActivity : BaseActivity(),
    PoaWalletValidationView {

  private lateinit var presenter: PoaWalletValidationPresenter
  @Inject
  lateinit var smsValidationRepository: SmsValidationRepositoryType
  @Inject
  lateinit var walletService: WalletService
  private var walletValidated: Boolean = false

  companion object {
    private const val RESULT_OK = 0
    private const val RESULT_CANCELED = 1
    private const val RESULT_FAILED = 2
    private const val WALLET_VALIDATED_KEY = "wallet_validated"

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, PoaWalletValidationActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_poa_wallet_validation)
    savedInstanceState?.let {
      walletValidated = it.getBoolean(WALLET_VALIDATED_KEY, false)
    }
    presenter = PoaWalletValidationPresenter(this, smsValidationRepository, walletService,
        CompositeDisposable(), AndroidSchedulers.mainThread(),
        Schedulers.io())
    presenter.present()
  }

  override fun onBackPressed() {
    if (walletValidated) {
      closeSuccess()
    } else {
      closeCancel(false)
    }
    super.onBackPressed()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
    super.onSaveInstanceState(outState, outPersistentState)
    outState?.putBoolean(
        WALLET_VALIDATED_KEY, walletValidated)
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?,
                                       errorMessage: Int?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PoaPhoneValidationFragment.newInstance(
                countryCode, phoneNumber, errorMessage))
        .commit()
  }

  override fun showCodeValidationView(countryCode: String, phoneNumber: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PoaCodeValidationFragment.newInstance(
                countryCode, phoneNumber))
        .commit()
  }

  override fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PoaCodeValidationFragment.newInstance(validationInfo, errorMessage))
        .commit()
  }

  override fun showLoading(it: ValidationInfo) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PoaValidationLoadingFragment.newInstance(it))
        .commit()
  }

  override fun showSuccess() {
    walletValidated = true
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PoaValidationSuccessFragment.newInstance())
        .commit()
  }

  override fun closeSuccess() {
    val intent = Intent()
    setResult(RESULT_OK, intent)
    finishAndRemoveTask()
  }

  override fun closeCancel(removeTask: Boolean) {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    if (removeTask) {
      finishAndRemoveTask()
    } else {
      finish()
    }
  }

  override fun closeError() {
    val intent = Intent()
    setResult(RESULT_FAILED, intent)
    finishAndRemoveTask()
  }

  override fun showCreateAnimation() {
    create_wallet_card.visibility = VISIBLE
    create_wallet_animation.visibility = VISIBLE
    create_wallet_animation.playAnimation()
    create_wallet_text.visibility = VISIBLE
  }

  override fun hideAnimation() {
    create_wallet_card.visibility = GONE
    create_wallet_animation.visibility = GONE
    create_wallet_text.visibility = GONE
  }
}