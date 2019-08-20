package com.asfoundation.wallet.wallet_validation.generic

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_wallet_validation.*
import kotlinx.android.synthetic.main.layout_referral_status.*
import javax.inject.Inject

class WalletValidationActivity : BaseActivity(),
    WalletValidationView {

  private lateinit var presenter: WalletValidationPresenter
  @Inject
  lateinit var smsValidationRepository: SmsValidationRepositoryType
  @Inject
  lateinit var walletInteractor: FindDefaultWalletInteract
  @Inject
  lateinit var createWalletInteractor: CreateWalletInteract

  private var minFrame = 0
  private var maxFrame = 30
  private var loopAnimation = -1

  companion object {
    const val FRAME_RATE = 30

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WalletValidationActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_validation)
    presenter = WalletValidationPresenter(this,
        smsValidationRepository, walletInteractor,
        createWalletInteractor, CompositeDisposable(), AndroidSchedulers.mainThread(),
        Schedulers.io())

    setupUI()

    presenter.present()
  }

  private fun setupUI() {
    validation_progress_animation!!.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation!!.repeatCount = loopAnimation
    validation_progress_animation!!.playAnimation()
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?,
                                       errorMessage: Int?) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PhoneValidationFragment.newInstance(
                countryCode, phoneNumber, errorMessage))
        .commit()
  }

  override fun showCodeValidationView(countryCode: String, phoneNumber: String) {
    increaseAnimationFrames()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(countryCode, phoneNumber))
        .commit()
  }

  override fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int) {
    updateAnimation()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(validationInfo, errorMessage))
        .commit()
  }

  override fun showLastStepAnimation() {
    increaseAnimationFrames()
  }

  override fun showTransactionsActivity() {
    startActivity(TransactionsActivity.newIntent(this))
    finish()
  }

  private fun updateAnimation() {
    validation_progress_animation!!.removeAllAnimatorListeners()
    validation_progress_animation!!.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation!!.repeatCount = loopAnimation
    validation_progress_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        minFrame += FRAME_RATE
        maxFrame += FRAME_RATE
        loopAnimation = -1
        validation_progress_animation!!.setMinAndMaxFrame(minFrame, maxFrame)
        validation_progress_animation!!.repeatCount = loopAnimation
        validation_progress_animation!!.playAnimation()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
    validation_progress_animation.playAnimation()
  }

  private fun increaseAnimationFrames() {
    minFrame += FRAME_RATE
    maxFrame += FRAME_RATE
    loopAnimation = 0
    updateAnimation()
  }

  override fun onDestroy() {
    presenter.stop()

    validation_progress_animation?.removeAllUpdateListeners()
    validation_progress_animation?.removeAllLottieOnCompositionLoadedListener()

    referral_status_animation?.removeAllUpdateListeners()
    referral_status_animation?.removeAllLottieOnCompositionLoadedListener()

    super.onDestroy()
  }
}