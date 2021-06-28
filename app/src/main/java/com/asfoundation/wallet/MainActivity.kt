package com.asfoundation.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.setupWithNavController
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
class MainActivity : DaggerAppCompatActivity() {


  companion object {
    private const val TAG = "MainActivity"

    fun newIntent(context: Context, supportNotificationClicked: Boolean): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      }
    }
  }

  enum class BottomNavItem(val navGraphIdRes: Int) {
    HOME(R.navigation.home_graph),
    PROMOTIONS(R.navigation.promotions_graph),
    MY_WALLETS(R.navigation.my_wallets_graph)
  }

  private val bottomNavItems =
      listOf(BottomNavItem.HOME, BottomNavItem.PROMOTIONS, BottomNavItem.MY_WALLETS)

  @Inject
  lateinit var navigator: MainActivityNavigator

  private var currentNavController: LiveData<NavController>? = null

  private var bottomNavigationView: BottomNavigationView? = null

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
    bottomNavigationView = findViewById(R.id.bottom_nav)

    val navGraphIds = bottomNavItems.map { it.navGraphIdRes }

    val controller = bottomNavigationView?.setupWithNavController(
        navGraphIds = navGraphIds,
        fragmentManager = supportFragmentManager,
        containerId = R.id.nav_host_container,
        intent = intent
    )

    setupTopUpItem(bottomNavigationView)

    currentNavController = controller
  }

  fun setSelectedBottomNavItem(bottomNavItem: BottomNavItem) {
    try {
      val index = bottomNavItems.indexOf(bottomNavItem)
      val menuItemId = bottomNavigationView?.menu?.get(index)?.itemId
      menuItemId?.let { bottomNavigationView?.selectedItemId = it }
    } catch (e: Exception) {
      Log.e(TAG, "Couldn't select nav item. " + e.message)
    }
  }

  private fun setupTopUpItem(bottomNavigationView: BottomNavigationView?) {
    val menuView = bottomNavigationView?.getChildAt(0) as BottomNavigationMenuView
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