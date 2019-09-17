package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_rewards_level.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*

class RewardsLevelActivity : BaseActivity(), GamificationView {

  private lateinit var menu: Menu
  private lateinit var presenter: RewardsLevelPresenter
  private var infoButtonSubject: PublishSubject<Any>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_rewards_level)
    toolbar()
    infoButtonSubject = PublishSubject.create()
    val fragment = MyLevelFragment()
    presenter = RewardsLevelPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    presenter.present()
    // Display the fragment as the main content.
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container, fragment)
        .commit()
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
    this.menu.findItem(R.id.action_info)
        .isVisible = true
    return super.onCreateOptionsMenu(menu)
  }

  override fun getInfoButtonClick(): Observable<Any> {
    return infoButtonSubject!!
  }

  override fun retryClick(): Observable<Any> {
    return RxView.clicks(retry_button)
  }

  override fun loadMyLevelFragment() {
    val fragment = MyLevelFragment()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
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
    infoButtonSubject = null
    presenter.stop()
    super.onDestroy()
  }
}