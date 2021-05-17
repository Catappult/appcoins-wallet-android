package com.asfoundation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.setupWithNavController
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
class MainActivity : DaggerAppCompatActivity() {

  @Inject
  lateinit var navigator: MainActivityNavigator
  private var currentNavController: LiveData<NavController>? = null

  companion object {
    fun newIntent(context: Context, supportNotificationClicked: Boolean): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      setupBottomNavigationBar()
    }
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    setupBottomNavigationBar()
  }

  /**
   * Note that this is setup this way because there's no official support for backstack with
   * bottom navigation yet. We should simplify once it's supported (next releases will be).
   *
   * Issue here https://issuetracker.google.com/issues/80029773#comment136
   */
  private fun setupBottomNavigationBar() {
    val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

    val navGraphIds =
        listOf(R.navigation.home_graph, R.navigation.promotions_graph, R.navigation.balance_graph)

    val controller = bottomNavigationView.setupWithNavController(
        navGraphIds = navGraphIds,
        fragmentManager = supportFragmentManager,
        containerId = R.id.nav_host_container,
        intent = intent
    )
    setupTopUpItem(bottomNavigationView)

    currentNavController = controller
  }

  private fun setupTopUpItem(bottomNavigationView: BottomNavigationView) {
    val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
    val topUpItemView = menuView.getChildAt(3) as BottomNavigationItemView
    val topUpCustomView = LayoutInflater.from(this)
        .inflate(R.layout.bottom_nav_top_up_item, menuView, false)
    topUpCustomView.setOnClickListener {
      navigator.navigateToTopUp()
    }
    topUpItemView.addView(topUpCustomView)
  }

  override fun onSupportNavigateUp(): Boolean {
    return currentNavController?.value?.navigateUp() ?: false
  }
}