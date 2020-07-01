package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_rewards_level.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*

class RewardsLevelActivity : BaseActivity(), RewardsLevelView {

  private lateinit var menu: Menu
  private lateinit var presenter: RewardsLevelPresenter
  private var toolbar: Toolbar? = null
  private var infoButtonSubject: PublishSubject<Any>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_rewards_level)
    toolbar = toolbar()
    setTitle("Get $bonus% Bonus")
    infoButtonSubject = PublishSubject.create()
    presenter =
        RewardsLevelPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    presenter.present(legacy)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        return true
      }

      R.id.action_info -> {
        infoButtonSubject?.onNext(Any())
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_info, menu)
    this.menu = menu
    if (legacy) this.menu.findItem(R.id.action_info).isVisible = true
    return super.onCreateOptionsMenu(menu)
  }

  override fun getInfoButtonClick() = infoButtonSubject!!

  override fun retryClick() = RxView.clicks(retry_button)

  override fun loadLegacyGamificationView() = navigateTo(LegacyGamificationFragment())

  override fun loadGamificationView() {
    toolbar?.menu?.removeItem(R.id.action_info)
    navigateTo(GamificationFragment())
  }

  override fun showNetworkErrorView() {
    gamification_no_network.visibility = View.VISIBLE
    retry_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
    fragment_container.visibility = View.GONE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun showMainView() {
    fragment_container.visibility = View.VISIBLE
    gamification_no_network.visibility = View.GONE
  }

  override fun onDestroy() {
    infoButtonSubject = null
    presenter.stop()
    super.onDestroy()
  }

  private fun navigateTo(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit()
  }

  private val legacy: Boolean by lazy {
    intent.getBooleanExtra(LEGACY, false)
  }

  private val bonus: Int by lazy {
    intent.getDoubleExtra(BONUS, 25.0)
        .toInt()
  }


  companion object {

    const val LEGACY = "legacy"
    const val BONUS = "bonus"

    @JvmStatic
    fun newIntent(context: Context, legacy: Boolean, bonus: Double): Intent {
      return Intent(context, RewardsLevelActivity::class.java).apply {
        putExtra(LEGACY, legacy)
        putExtra(BONUS, bonus)
      }
    }
  }
}