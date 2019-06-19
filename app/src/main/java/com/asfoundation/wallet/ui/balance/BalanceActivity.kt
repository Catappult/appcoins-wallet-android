package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.asf.wallet.R
import com.asfoundation.wallet.router.TopUpRouter
import com.asfoundation.wallet.router.TransactionsRouter


class BalanceActivity : BaseActivity(),
    BalanceActivityView {

  private lateinit var presenter: BalancePresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, BalanceActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    val fade = Fade()
    fade.duration = 500
    window.enterTransition = fade

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_balance)
    presenter = BalancePresenter(this)
    presenter.present(savedInstanceState)
  }

  override fun onBackPressed() {
    TransactionsRouter().open(this, true)
    finish()
  }

  override fun showBalanceScreen() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            BalanceFragment.newInstance())
        .addToBackStack(BalanceFragment::class.java.simpleName).commit()
  }

  override fun showTokenDetailsScreen(
      tokenDetailsId: TokenDetailsActivity.TokenDetailsId, imgView: ImageView,
      textView: TextView, parentView: View) {

    val intent = TokenDetailsActivity.newInstance(this,
        tokenDetailsId)

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

  override fun setupToolbar() {
    toolbar()
  }
}