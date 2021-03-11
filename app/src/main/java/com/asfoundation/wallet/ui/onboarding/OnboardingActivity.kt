package com.asfoundation.wallet.ui.onboarding

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject

class OnboardingActivity : BaseActivity(), OnboardingView {

  @Inject
  lateinit var presenter: OnboardingPresenter

  private lateinit var listener: OnboardingPageChangeListener
  private lateinit var adapter: OnboardingPageAdapter
  private var linkSubject: PublishSubject<String>? = null
  private var paymentMethodsIcons: ArrayList<String> = ArrayList()

  companion object {

    @JvmStatic
    fun newIntent(context: Context, fromSupportNotification: Boolean = false): Intent {
      val intent = Intent(context, OnboardingActivity::class.java)
      intent.putExtra(SUPPORT_NOTIFICATION_CLICK, fromSupportNotification)
      return intent
    }
    // NOTE - if this gets moved to myappcoins domain, don't forget to add "lang" parameter to both
    //  these urls. See how it's done in SettingsFragment
    private const val TERMS_CONDITIONS_URL = "https://catappult.io/appcoins-wallet/terms-conditions"
    private const val PRIVACY_POLICY_URL = "https://catappult.io/appcoins-wallet/privacy-policy"
    private const val PAYMENT_METHODS_ICONS = "paymentMethodsIcons"
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putStringArrayList(PAYMENT_METHODS_ICONS, paymentMethodsIcons)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.activity_onboarding)
    linkSubject = PublishSubject.create()
    setupUI(savedInstanceState)
    presenter.present()
  }

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  override fun onDestroy() {
    linkSubject = null
    presenter.stop()
    create_wallet_animation.removeAllAnimatorListeners()
    create_wallet_animation.removeAllUpdateListeners()
    create_wallet_animation.removeAllLottieOnCompositionLoadedListener()
    super.onDestroy()
  }

  private fun setupUI(savedInstanceState: Bundle?) {
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

    adapter = OnboardingPageAdapter(createDefaultItemList())

    onboarding_viewpager.setPageTransformer(OnboardingPageTransformer())
    onboarding_viewpager.adapter = adapter
    savedInstanceState?.getStringArrayList(PAYMENT_METHODS_ICONS)
        ?.let { paymentMethodsIcons = it }
    listener = OnboardingPageChangeListener(onboarding_content, paymentMethodsIcons)
    onboarding_viewpager.registerOnPageChangeCallback(listener)

    onboarding_content.visibility = View.VISIBLE
    wallet_creation_animation.visibility = View.GONE
  }

  override fun getNextButtonClick() = RxView.clicks(next_button)

  override fun getSkipClicks() = RxView.clicks(skip_button)

  override fun showViewPagerLastPage() {
    onboarding_viewpager.setCurrentItem(onboarding_viewpager.adapter?.itemCount ?: 0, true)
  }

  override fun setPaymentMethodsIcons(paymentMethodsIcons: List<String>) {
    this.paymentMethodsIcons = ArrayList(paymentMethodsIcons)
    listener.setPaymentMethodsIcons(paymentMethodsIcons)
  }

  override fun getLinkClick() = linkSubject!!

  override fun showWarningText() {
    if (!onboarding_checkbox.isChecked &&
        terms_conditions_warning.visibility == View.INVISIBLE &&
        terms_conditions_layout.visibility == View.VISIBLE) {
      animateShowWarning(terms_conditions_warning)
      terms_conditions_warning.visibility = View.VISIBLE
      presenter.markedWarningTextAsShowed()
    }
  }

  private fun animateShowWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fast_fade_in_animation)
    textView.animation = animation
  }

  override fun showLoading() {
    onboarding_content.visibility = View.GONE
    wallet_creation_animation.visibility = View.VISIBLE
    create_wallet_animation.setAnimation(R.raw.create_wallet_loading_animation)
    create_wallet_animation.playAnimation()
  }

  override fun finishOnboarding(showAnimation: Boolean) {
    create_wallet_animation.setAnimation(R.raw.success_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit
      override fun onAnimationEnd(animation: Animator?) = presenter.endOnboarding()
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
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
        ds.color = ResourcesCompat.getColor(resources, R.color.grey_alpha_active_54, null)
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

  private fun createDefaultItemList(): List<OnboardingItem> {
    val item1 = OnboardingItem(R.string.intro_1_title, this.getString(R.string.intro_1_body))
    val item2 = OnboardingItem(R.string.intro_2_title, this.getString(R.string.intro_2_body))
    val item3 = OnboardingItem(R.string.intro_3_title, this.getString(R.string.intro_3_body))
    return listOf(item1, item2, item3)
  }
}
