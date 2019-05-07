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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.R
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject

class OnboardingActivity : BaseActivity(), OnboardingView {

  @Inject
  lateinit var interactor: CreateWalletInteract
  @Inject
  lateinit var service: WalletService
  private lateinit var presenter: OnboardingPresenter

  companion object {
    fun newInstance(): OnboardingActivity {
      return OnboardingActivity()
    }

    const val TERMS_CONDITIONS_URL = "https://catappult.io/appcois-wallet/terms-conditions"
    const val PRIVACY_POLICY_URL = "https://catappult.io/appcois-wallet/privacy-policy"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.activity_onboarding)
    presenter = OnboardingPresenter(CompositeDisposable(), this, interactor, service,
        AndroidSchedulers.mainThread())
  }

  override fun onResume() {
    super.onResume()
    presenter.present()
  }

  override fun onDestroy() {
    presenter.stop()
    create_wallet_animation.removeAllAnimatorListeners()
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

    intro.setPageTransformer(false, DepthPageTransformer())
    intro.adapter = IntroPagerAdapter()
    intro.addOnPageChangeListener(PageChangeListener(onboarding_content))
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(ok_action)
  }

  override fun getSkipClick(): Observable<Any> {
    return RxView.clicks(skip_action)
  }

  override fun getCheckboxClick(): Observable<Any> {
    return RxView.clicks(onboarding_checkbox)
  }

  override fun showLoading() {
    onboarding_content.visibility = View.GONE
    wallet_creation_animation.visibility = View.VISIBLE
    create_wallet_animation.playAnimation()

  }

  override fun finishOnboarding() {
    create_wallet_animation.setAnimation(R.raw.create_wallet_finish_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        TransactionsRouter().open(applicationContext, true)
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
        val launchBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(launchBrowser)
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
}

class DepthPageTransformer : ViewPager.PageTransformer {

  override fun transformPage(view: View, position: Float) {
    val pageWidth = view.width

    when {
      position < -1 -> // [-Infinity,-1)
        // This page is way off-screen to the left.
        view.alpha = 0f
      position <= 0 -> { // [-1,0]
        // Use the default slide transition when moving to the left page
        view.alpha = 1f
        view.translationX = 0f
        view.scaleX = 1f
        view.scaleY = 1f
      }
      position <= 1 -> { // (0,1]
        // Fade the page out.
        view.alpha = 1 - position

        // Counteract the default slide transition
        view.translationX = pageWidth * -position

        // Scale the page down (between MIN_SCALE and 1)
        val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
        view.scaleX = scaleFactor
        view.scaleY = scaleFactor
      }
      else -> // (1,+Infinity]
        // This page is way off-screen to the right.
        view.alpha = 0f
    }
  }

  companion object {
    private const val MIN_SCALE = 0.75f
  }
}

class IntroPagerAdapter : PagerAdapter() {
  private val titles =
      intArrayOf(R.string.intro_title_first_page, R.string.intro_2_title, R.string.intro_3_title,
          R.string.intro_4_title)
  private val messages =
      intArrayOf(R.string.intro_1_body, R.string.intro_2_body, R.string.intro_3_body,
          R.string.intro_4_body)

  override fun getCount(): Int {
    return titles.size
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val view = LayoutInflater.from(container.context)
        .inflate(R.layout.layout_page_intro, container, false)
    (view.findViewById<View>(R.id.title) as TextView).setText(titles[position])
    (view.findViewById<View>(R.id.message) as TextView).setText(messages[position])
    container.addView(view)
    return view
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as View)
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return view === `object`
  }
}

class PageChangeListener internal constructor(private val view: View) :
    ViewPager.OnPageChangeListener {

  companion object {
    var ANIMATION_TRANSITIONS = 3
  }

  private var lottieView: LottieAnimationView? = null
  private var skipButton: Button? = null
  private var okButton: Button? = null
  private var checkBox: CheckBox? = null
  private var warningText: TextView? = null
  private var termsConditionsLayout: LinearLayout? = null

  init {
    init()
  }

  fun init() {
    lottieView = view.findViewById(R.id.lottie_onboarding)
    skipButton = view.findViewById(R.id.skip_action)
    okButton = view.findViewById(R.id.ok_action)
    checkBox = view.findViewById(R.id.onboarding_checkbox)
    warningText = view.findViewById(R.id.terms_conditions_warning)
    termsConditionsLayout = view.findViewById(R.id.terms_conditions_layout)
  }

  private fun showWarningText(position: Int) {
    if (!checkBox!!.isChecked && position == 3) {
      animateShowWarning(warningText!!)
      warningText!!.visibility = View.VISIBLE
    } else {
      if (warningText!!.visibility == View.VISIBLE) {
        animateHideWarning(warningText!!)
        warningText!!.visibility = View.GONE
      }
    }
  }

  private fun showSkipButton(position: Int) {
    if (Math.floor(position.toDouble()) != 3.0 && checkBox!!.isChecked) {
      if (skipButton!!.visibility != View.VISIBLE) {
        animateShowButton(skipButton!!)
        animateCheckboxUp(termsConditionsLayout!!)
        skipButton!!.visibility = View.VISIBLE
      }
    } else {
      if (skipButton!!.visibility == View.VISIBLE) {
        animateHideButton(skipButton!!)
        animateCheckboxDown(termsConditionsLayout!!)
        skipButton!!.visibility = View.GONE
      }
    }
  }

  private fun showOkButton(position: Int) {
    if (checkBox!!.isChecked && position == 3) {
      animateShowButton(okButton!!)
      animateCheckboxUp(termsConditionsLayout!!)
      okButton!!.visibility = View.VISIBLE
    } else {
      if (okButton!!.visibility == View.VISIBLE) {
        animateHideButton(okButton!!)
        animateCheckboxDown(termsConditionsLayout!!)
        okButton!!.visibility = View.GONE
      }
    }
  }

  private fun animateCheckboxUp(layout: LinearLayout) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.minor_translate_up)
    animation.fillAfter = true
    layout.animation = animation
  }

  private fun animateCheckboxDown(layout: LinearLayout) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.minor_translate_down)
    animation.fillAfter = true
    layout.animation = animation
  }

  private fun animateShowButton(button: Button) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.bottom_translate_in)
    button.animation = animation
  }

  private fun animateShowWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_in_animation)
    textView.animation = animation
  }

  private fun animateHideButton(button: Button) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.bottom_translate_out)
    button.animation = animation
  }

  private fun animateHideWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_out_animation)
    textView.animation = animation
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    lottieView!!.progress =
        position * (1f / ANIMATION_TRANSITIONS) + positionOffset * (1f / ANIMATION_TRANSITIONS)
    checkBox!!.setOnClickListener { view ->
      showWarningText(position)
      showSkipButton(position)
      showOkButton(position)
    }
    showWarningText(position)
    showSkipButton(position)
    showOkButton(position)
  }

  override fun onPageSelected(position: Int) {

  }

  override fun onPageScrollStateChanged(state: Int) {

  }
}