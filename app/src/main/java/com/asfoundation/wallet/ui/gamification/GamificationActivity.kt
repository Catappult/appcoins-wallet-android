package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
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

  private var _binding: ActivityRewardsLevelBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // activity_rewards_level.xml
  private val gamification_no_network get() = binding.gamificationNoNetwork
  private val fragment_container get() = binding.fragmentContainer

  // no_network_retry_only_layout.xml
  private val retry_button get() = binding.noNetworkRetryOnlyLayout.retryButton
  private val retry_animation get() = binding.noNetworkRetryOnlyLayout.retryAnimation

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityRewardsLevelBinding.inflate(layoutInflater)
    setContentView(binding.root)
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

  override fun retryClick() = RxView.clicks(retry_button)

  override fun loadGamificationView() {
    toolbar?.menu?.removeItem(R.id.action_info)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, GamificationFragment())
        .commit()
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