package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityRewardsLevelBinding
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

@AndroidEntryPoint
class GamificationActivity : BaseActivity(), GamificationActivityView {

  private lateinit var menu: Menu
  private lateinit var presenter: GamificationActivityPresenter
  private var toolbar: Toolbar? = null
  private var backEnabled = true
  private var onBackPressedSubject: PublishSubject<Any>? = null

  private val binding by viewBinding(ActivityRewardsLevelBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_rewards_level)
    toolbar = toolbar()
    onBackPressedSubject = PublishSubject.create()
    setTitle(getString(R.string.gamif_title, bonus.toString()))
    presenter =
        GamificationActivityPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    presenter.present()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        if (backEnabled) {
          onBackPressed()
        } else {
          onBackPressedSubject?.onNext("")
        }
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_info, menu)
    this.menu = menu
    return super.onCreateOptionsMenu(menu)
  }

  override fun retryClick() = RxView.clicks(binding.noNetworkRetryOnlyLayout.retryButton)

  override fun loadGamificationView() {
    toolbar?.menu?.removeItem(R.id.action_info)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, GamificationFragment())
        .commit()
  }

  override fun showNetworkErrorView() {
    binding.gamificationNoNetwork.visibility = View.VISIBLE
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.VISIBLE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.GONE
    binding.fragmentContainer.visibility = View.GONE
  }

  override fun showRetryAnimation() {
    binding.noNetworkRetryOnlyLayout.retryButton.visibility = View.INVISIBLE
    binding.noNetworkRetryOnlyLayout.retryAnimation.visibility = View.VISIBLE
  }

  override fun showMainView() {
    binding.fragmentContainer.visibility = View.VISIBLE
    binding.gamificationNoNetwork.visibility = View.GONE
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  private val bonus: Int by lazy {
    intent.getDoubleExtra(BONUS, 25.0)
        .toInt()
  }

  companion object {
    const val BONUS = "bonus"

    @JvmStatic
    fun newIntent(context: Context, bonus: Double): Intent {
      return Intent(context, GamificationActivity::class.java).apply {
        putExtra(BONUS, bonus)
      }
    }
  }

  override fun backPressed() = onBackPressedSubject!!

  override fun enableBack() {
    backEnabled = true
  }

  override fun disableBack() {
    backEnabled = false
  }
}