package com.asfoundation.wallet.ui.balance

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.asfoundation.wallet.navigator.ActivityNavigator
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.backup.BackupActivity.Companion.newIntent
import com.asfoundation.wallet.ui.wallets.RemoveWalletActivity
import com.asfoundation.wallet.ui.wallets.WalletDetailsFragment
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_balance.*
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*


class BalanceActivity : ActivityNavigator(), BalanceActivityView {

  private lateinit var activityPresenter: BalanceActivityPresenter
  private var onBackPressedSubject: PublishSubject<Any>? = null
  private var backEnabled = true
  private var expandBottomSheet: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    val fade = Fade()
    fade.duration = 500
    window.enterTransition = fade

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_balance)
    onBackPressedSubject = PublishSubject.create()
    activityPresenter = BalanceActivityPresenter(this)
    activityPresenter.present(savedInstanceState)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (wallet_remove_animation == null || wallet_creation_animation.visibility != View.VISIBLE) {
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
    if (wallet_remove_animation == null || wallet_remove_animation.visibility != View.VISIBLE) super.onBackPressed()
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
        androidx.core.util.Pair(imgView, ViewCompat.getTransitionName(imgView)!!),
        androidx.core.util.Pair(textView, ViewCompat.getTransitionName(textView)!!),
        androidx.core.util.Pair(parentView,
            ViewCompat.getTransitionName(parentView)!!))

    startActivity(intent, options.toBundle())

  }

  override fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean) {
    expandBottomSheet = true
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletDetailsFragment.newInstance(walletAddress, isActive))
        .addToBackStack(WalletDetailsFragment::class.java.simpleName)
        .commit()
  }

  override fun navigateToRemoveWalletView(walletAddress: String, totalFiatBalance: String,
                                          appcoinsBalance: String, creditsBalance: String,
                                          ethereumBalance: String) {
    startActivityForResult(
        RemoveWalletActivity.newIntent(this, walletAddress, totalFiatBalance, appcoinsBalance,
            creditsBalance, ethereumBalance), REQUEST_CODE)
  }

  override fun navigateToBackupView(walletAddress: String) {
    startActivity(newIntent(this, walletAddress))
  }

  override fun navigateToRestoreView() {
    startActivity(RestoreWalletActivity.newIntent(this))
  }

  override fun showCreatingAnimation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    wallet_creation_animation.visibility = View.VISIBLE
    create_wallet_animation.playAnimation()
  }

  override fun showWalletCreatedAnimation() {
    create_wallet_animation.setAnimation(R.raw.success_animation)
    create_wallet_text.text = getText(R.string.provide_wallet_created_header)
    create_wallet_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit
      override fun onAnimationEnd(animation: Animator?) = navigateToTransactions()
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
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
    setTitle(getString(R.string.bottom_navigation_my_wallets))
  }

  override fun enableBack() {
    backEnabled = true
  }

  override fun disableBack() {
    backEnabled = false
  }

  override fun backPressed() = onBackPressedSubject!!

  override fun navigateToTransactions() {
    TransactionsRouter().open(this, false)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      expandBottomSheet = true
      showBalanceScreen()
    }
  }

  companion object {
    private const val REQUEST_CODE = 123

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, BalanceActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    }
  }
}