package com.asfoundation.wallet.ui.onboarding

import android.animation.Animator
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_onboarding.*
import kotlinx.android.synthetic.main.layout_validation_no_internet.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OnboardingActivity : BaseActivity(), OnboardingView {

  @Inject
  lateinit var interactor: OnboardingInteract
  @Inject
  lateinit var smsValidationInteract: SmsValidationInteract
  private lateinit var browserRouter: ExternalBrowserRouter
  private lateinit var presenter: OnboardingPresenter
  private var linkSubject: PublishSubject<String>? = null

  companion object {
    fun newInstance(): OnboardingActivity {
      return OnboardingActivity()
    }

    const val TERMS_CONDITIONS_URL = "https://catappult.io/appcoins-wallet/terms-conditions"
    const val PRIVACY_POLICY_URL = "https://catappult.io/appcoins-wallet/privacy-policy"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.activity_onboarding)
    browserRouter = ExternalBrowserRouter()
    linkSubject = PublishSubject.create()
    presenter = OnboardingPresenter(CompositeDisposable(), this, interactor,
        AndroidSchedulers.mainThread(), smsValidationInteract, Schedulers.io(),
        PublishSubject.create())
    setupUi()
  }

  override fun onResume() {
    presenter.present()
    super.onResume()
  }

  override fun onPause() {
    presenter.stop()
    create_wallet_animation.removeAllAnimatorListeners()
    super.onPause()
  }

  override fun onDestroy() {
    linkSubject = null
    create_wallet_animation.removeAllUpdateListeners()
    create_wallet_animation.removeAllLottieOnCompositionLoadedListener()
    super.onDestroy()
  }

  override fun setupUi() {
    val termsConditions = resources.getString(R.string.terms_and_conditions)
    val privacyPolicy = resources.getString(R.string.privacy_policy)
    val termsPolicyTickBox =
        resources.getString(R.string.terms_and_conditions_tickbox, termsConditions,
            privacyPolicy)

    val spannableString = SpannableString(termsPolicyTickBox)
    setLinkToString(spannableString, termsConditions, TERMS_CONDITIONS_URL)
    setLinkToString(spannableString, privacyPolicy, PRIVACY_POLICY_URL)

    terms_conditions_body.text = spannableString
    terms_conditions_body.isClickable = true
    terms_conditions_body.movementMethod = LinkMovementMethod.getInstance()

    onboarding_viewpager.setPageTransformer(OnboardingPageTransformer())
    onboarding_viewpager.adapter = OnboardingPageAdapter()
    onboarding_viewpager.registerOnPageChangeCallback(
        OnboardingPageChangeListener(onboarding_content))

    onboarding_content.visibility = View.VISIBLE
    wallet_creation_animation.visibility = View.GONE
    layout_validation_no_internet.visibility = View.GONE
  }

  override fun getNextButtonClick(): Observable<Any> {
    return RxView.clicks(next_button)
  }

  override fun getRedeemButtonClick(): Observable<Any> {
    return RxView.clicks(redeem_bonus)
  }

  override fun getSkipClicks(): Observable<Any> {
    return RxView.clicks(skip_button)
  }

  override fun showViewPagerLastPage() {
    onboarding_viewpager.setCurrentItem(onboarding_viewpager.adapter?.itemCount ?: 0, true)
  }

  override fun getLinkClick(): Observable<String> {
    return linkSubject!!
  }

  override fun showLoading() {
    onboarding_content.visibility = View.GONE
    wallet_creation_animation.visibility = View.VISIBLE
    create_wallet_animation.setAnimation(R.raw.create_wallet_loading_animation)
    create_wallet_animation.playAnimation()
  }

  private fun navigate(walletValidationStatus: WalletValidationStatus?) {
    if (walletValidationStatus == null || walletValidationStatus == WalletValidationStatus.SUCCESS) {
      TransactionsRouter().open(this, true)
    } else {
      val intent = WalletValidationActivity.newIntent(this)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(intent)
    }
    presenter.markOnboardingCompleted()
  }

  override fun finishOnboarding(walletValidationStatus: WalletValidationStatus,
                                showAnimation: Boolean) {
    if (!showAnimation) {
      navigate(walletValidationStatus)
      return
    }
    create_wallet_animation.setAnimation(R.raw.success_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        navigate(walletValidationStatus)
        finish()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
    create_wallet_animation.repeatCount = 0
    create_wallet_animation.playAnimation()
  }

  private fun setLinkToString(spannableString: SpannableString, highlightString: String,
                              uri: String) {
    val clickableSpan = object : ClickableSpan() {
      override fun onClick(widget: View) {
        linkSubject?.onNext(uri)
      }

      override fun updateDrawState(ds: TextPaint) {
        ds.color = resources.getColor(R.color.grey_8a_alpha)
        ds.isUnderlineText = true
      }
    }
    val indexHighlightString = spannableString.toString()
        .indexOf(highlightString)
    val highlightStringLength = highlightString.length
    spannableString.setSpan(clickableSpan, indexHighlightString,
        indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(StyleSpan(Typeface.BOLD), indexHighlightString,
        indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  }

  override fun navigateToBrowser(uri: Uri) {
    browserRouter.open(this, uri)
  }

  override fun showNoInternetView() {
    stopRetryAnimation()
    onboarding_content.visibility = View.GONE
    wallet_creation_animation.visibility = View.GONE
    layout_validation_no_internet.visibility = View.VISIBLE
  }

  override fun getRetryButtonClicks(): Observable<Any> {
    return RxView.clicks(retry_button)
        .doOnNext { playRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
  }

  override fun getLaterButtonClicks(): Observable<Any> {
    return RxView.clicks(later_button)
  }

  private fun playRetryAnimation() {
    retry_button.visibility = View.GONE
    later_button.visibility = View.GONE
    retry_animation.visibility = View.VISIBLE
    retry_animation.playAnimation()
  }

  private fun stopRetryAnimation() {
    retry_button.visibility = View.VISIBLE
    later_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
  }
}