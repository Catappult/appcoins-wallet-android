package com.asfoundation.wallet.ui.balance

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.asf.wallet.R
import com.asfoundation.wallet.router.TopUpRouter
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.wallets.WalletDetailFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_balance.*
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*


class BalanceActivity : BaseActivity(),
    BalanceActivityView {

  private lateinit var activityPresenter: BalanceActivityPresenter
  private var onBackPressedSubject: PublishSubject<Any>? = null
  private var backEnabled = false
  private var expandBottomSheet: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    val fade = Fade()
    fade.duration = 500
    window.enterTransition = fade

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_balance)
    expandBottomSheet = intent.extras!!.getBoolean(EXPAND_BOTTOM_SHEET_KEY, false)
    onBackPressedSubject = PublishSubject.create()
    activityPresenter = BalanceActivityPresenter(this)
    activityPresenter.present(savedInstanceState)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (wallet_creation_animation.visibility != View.VISIBLE) {
        if (backEnabled) {
          super.onBackPressed()
        } else {
          onBackPressedSubject?.onNext("")
        }
      }
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
  }

  override fun showBalanceScreen() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BalanceFragment.newInstance())
        .commit()
  }

  override fun showTokenDetailsScreen(
      tokenDetailsId: TokenDetailsActivity.TokenDetailsId, imgView: ImageView,
      textView: TextView, parentView: View) {

    val intent = TokenDetailsActivity.newInstance(this, tokenDetailsId)

    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
        androidx.core.util.Pair<View, String>(imgView, ViewCompat.getTransitionName(imgView)!!),
        androidx.core.util.Pair<View, String>(textView, ViewCompat.getTransitionName(textView)!!),
        androidx.core.util.Pair<View, String>(parentView,
            ViewCompat.getTransitionName(parentView)!!))

    startActivity(intent, options.toBundle())

  }

  override fun showTopUpScreen() {
    TopUpRouter().open(this)
  }

  override fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean) {
    expandBottomSheet = true
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, WalletDetailFragment.newInstance(walletAddress, isActive))
        .addToBackStack(WalletDetailFragment::class.java.simpleName)
        .commit()
  }

  override fun navigateToRemoveWalletView(walletAddress: String, totalFiatBalance: String,
                                          appcoinsBalance: String, creditsBalance: String,
                                          ethereumBalance: String) {
    startActivity(
        RemoveWalletActivity.newIntent(this, walletAddress, totalFiatBalance, appcoinsBalance,
            creditsBalance, ethereumBalance))
  }

  override fun navigateToBackupView(walletAddress: String) {

  }

  override fun showCreatingAnimation() {
    wallet_creation_animation.visibility = View.VISIBLE
    create_wallet_animation.playAnimation()
  }

  override fun showWalletCreatedAnimation() {
    create_wallet_animation.setAnimation(R.raw.success_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
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

  override fun shouldExpandBottomSheet(): Boolean {
    val shouldExpand = expandBottomSheet
    expandBottomSheet = false
    return shouldExpand
  }

  override fun setupToolbar() {
    toolbar()
  }

  override fun enableBack() {
    backEnabled = true
  }

  override fun disableBack() {
    backEnabled = false
  }

  override fun backPressed(): Observable<Any> {
    return onBackPressedSubject!!
  }

  override fun navigateToTransactions() {
    TransactionsRouter().open(this, true)
    finish()
  }

  companion object {
    private const val EXPAND_BOTTOM_SHEET_KEY = "expand_bottom_sheet"

    @JvmStatic
    fun newIntent(context: Context, expandBottomSheet: Boolean = false): Intent {
      val intent = Intent(context, BalanceActivity::class.java)
      intent.putExtra(EXPAND_BOTTOM_SHEET_KEY, expandBottomSheet)
      return intent
    }
  }
}