package com.asfoundation.wallet.wallet_validation.generic

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.topup.TopUpActivity.Companion.ERROR_MESSAGE
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_wallet_validation.*
import kotlinx.android.synthetic.main.layout_referral_status.*

class WalletValidationActivity : BaseActivity(),
    WalletValidationView {

  private lateinit var presenter: WalletValidationPresenter

  private var minFrame = 0
  private var maxFrame = 30
  private var loopAnimation = -1

  private val hasBeenInvitedFlow: Boolean by lazy {
    intent.getBooleanExtra(HAS_BEEN_INVITED_FLOW, false)
  }

  private val navigateToTransactionsOnSuccess: Boolean by lazy {
    intent.getBooleanExtra(NAVIGATE_TO_TRANSACTIONS_ON_SUCCESS, true)
  }

  private val navigateToTransactionsOnCancel: Boolean by lazy {
    intent.getBooleanExtra(NAVIGATE_TO_TRANSACTIONS_ON_CANCEL, true)
  }

  private val showToolbar: Boolean by lazy {
    intent.getBooleanExtra(SHOW_TOOLBAR, false)
  }

  private val previousContext: String by lazy {
    intent.getStringExtra(PREVIOUS_CONTEXT)
  }

  private val errorMessage: Int by lazy {
    intent.getIntExtra(ERROR_MESSAGE, 0)
  }


  companion object {
    const val FRAME_RATE = 30
    const val HAS_BEEN_INVITED_FLOW = "has_been_invited_flow"
    const val NAVIGATE_TO_TRANSACTIONS_ON_SUCCESS = "navigate_to_transactions_on_success"
    const val NAVIGATE_TO_TRANSACTIONS_ON_CANCEL = "navigate_to_transactions_on_cancel"
    const val SHOW_TOOLBAR = "show_toolbar"
    const val PREVIOUS_CONTEXT = "previous_context"
    const val MIN_FRAME = "minFrame"
    const val MAX_FRAME = "maxFrame"
    const val LOOP_ANIMATION = "loopAnimation"

    @JvmStatic
    fun newIntent(context: Context, hasBeenInvitedFlow: Boolean,
                  navigateToTransactionsOnSuccess: Boolean, navigateToTransactionsOnCancel: Boolean,
                  showToolbar: Boolean, previousContext: String): Intent {
      return Intent(context, WalletValidationActivity::class.java).apply {
        putExtra(HAS_BEEN_INVITED_FLOW, hasBeenInvitedFlow)
        putExtra(NAVIGATE_TO_TRANSACTIONS_ON_SUCCESS, navigateToTransactionsOnSuccess)
        putExtra(NAVIGATE_TO_TRANSACTIONS_ON_CANCEL, navigateToTransactionsOnCancel)
        putExtra(SHOW_TOOLBAR, showToolbar)
        putExtra(PREVIOUS_CONTEXT, previousContext)
      }
    }

    fun newIntent(context: Context, @StringRes error: Int): Intent {
      return Intent(context, WalletValidationActivity::class.java).apply {
        putExtra(HAS_BEEN_INVITED_FLOW, false)
        putExtra(NAVIGATE_TO_TRANSACTIONS_ON_SUCCESS, false)
        putExtra(NAVIGATE_TO_TRANSACTIONS_ON_CANCEL, false)
        putExtra(SHOW_TOOLBAR, true)
        putExtra(ERROR_MESSAGE, error)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putInt(MIN_FRAME, minFrame)
    outState.putInt(MAX_FRAME, maxFrame)
    outState.putInt(LOOP_ANIMATION, loopAnimation)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_validation)
    presenter = WalletValidationPresenter(this)

    handleSavedInstance(savedInstanceState)

    setupUI()

    presenter.present(savedInstanceState != null)
  }

  private fun handleSavedInstance(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      if (savedInstanceState.containsKey(MIN_FRAME)) {
        minFrame = it.getInt(MIN_FRAME)
      }
      if (savedInstanceState.containsKey(MAX_FRAME)) {
        maxFrame = it.getInt(MAX_FRAME)
      }
      if (savedInstanceState.containsKey(LOOP_ANIMATION)) {
        loopAnimation = it.getInt(LOOP_ANIMATION)
      }
    }
  }

  private fun setupUI() {
    validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.playAnimation()
    scrollView.isHorizontalScrollBarEnabled = false
    scrollView.isVerticalScrollBarEnabled = false
    setupToolbar()
  }

  private fun setupToolbar() {
    if (showToolbar) {
      toolbar()
      setTitle(getString(R.string.verification_settings_unverified_title))
      wallet_validation_toolbar.visibility = View.VISIBLE
    }
  }

  override fun showProgressAnimation() {
    validation_progress_animation.visibility = View.VISIBLE
  }

  override fun hideProgressAnimation() {
    validation_progress_animation.visibility = View.INVISIBLE
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?,
                                       errorMessage: Int?, isSavedInstance: Boolean) {
    if (!isSavedInstance) {
      if (countryCode != null && phoneNumber != null) {
        reverseAnimation(30, 60, 0)
      }
      supportFragmentManager.beginTransaction()
          .replace(R.id.fragment_container,
              PhoneValidationFragment.newInstance(
                  countryCode, phoneNumber, errorMessage, hasBeenInvitedFlow, previousContext))
          .commit()

      Handler().postDelayed({
        if (countryCode != null && phoneNumber != null) {
          reverseAnimation(0, 30, -1)
        }
      }, 1000)
    }
  }

  override fun showCodeValidationView(countryCode: String, phoneNumber: String) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(countryCode, phoneNumber, hasBeenInvitedFlow))
        .commit()
    increaseAnimationFrames()
  }

  override fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int) {
    updateAnimation()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(validationInfo, errorMessage, hasBeenInvitedFlow))
        .commit()
  }

  override fun showLastStepAnimation() {
    increaseAnimationFrames()
  }

  override fun finishSuccessActivity() {
    if (navigateToTransactionsOnSuccess) {
      startActivity(TransactionsActivity.newIntent(this))
    } else if (errorMessage != 0) {
      val intent = Intent().apply { putExtra(ERROR_MESSAGE, errorMessage) }
      setResult(RESULT_OK, intent)
    }
    finish()
  }

  override fun finishCancelActivity() {
    if (navigateToTransactionsOnCancel) {
      startActivity(TransactionsActivity.newIntent(this))
    } else if (errorMessage != 0) {
      val intent = Intent().apply { putExtra(ERROR_MESSAGE, errorMessage) }
      setResult(RESULT_CANCELED, intent)
    }
    finish()
  }

  private fun updateAnimation() {
    validation_progress_animation.removeAllAnimatorListeners()
    validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit

      override fun onAnimationEnd(animation: Animator?) {
        minFrame += FRAME_RATE
        maxFrame += FRAME_RATE
        loopAnimation = -1
        validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
        validation_progress_animation.repeatCount = loopAnimation
        validation_progress_animation.playAnimation()
      }

      override fun onAnimationCancel(animation: Animator?) = Unit

      override fun onAnimationStart(animation: Animator?) = Unit
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
    validation_progress_animation?.removeAllUpdateListeners()
    validation_progress_animation?.removeAllLottieOnCompositionLoadedListener()

    referral_status_animation?.removeAllUpdateListeners()
    referral_status_animation?.removeAllLottieOnCompositionLoadedListener()

    super.onDestroy()
  }

  private fun reverseAnimation(minFrame: Int, maxFrame: Int, loop: Int) {
    this.minFrame = minFrame
    this.maxFrame = maxFrame
    loopAnimation = loop

    validation_progress_animation.removeAllAnimatorListeners()
    validation_progress_animation.setMinAndMaxFrame(this.minFrame, this.maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.reverseAnimationSpeed()
    validation_progress_animation.playAnimation()
  }

}