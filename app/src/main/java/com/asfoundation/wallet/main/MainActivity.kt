package com.asfoundation.wallet.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.asf.wallet.R
import com.asfoundation.wallet.base.SingleStateFragment
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
class MainActivity : DaggerAppCompatActivity(), SingleStateFragment<MainState, MainSideEffect> {

  enum class BottomNavItem(val navGraphIdRes: Int) {
    HOME(R.navigation.home_graph),
    PROMOTIONS(R.navigation.promotions_graph),
    MY_WALLETS(R.navigation.my_wallets_graph)
  }

  private val bottomNavItems =
      listOf(BottomNavItem.HOME, BottomNavItem.PROMOTIONS, BottomNavItem.MY_WALLETS)

  private var promotionBadge: View? = null

  @Inject
  lateinit var navigator: MainActivityNavigator

  @Inject
  lateinit var mainViewModelFactory: MainViewModelFactory
  private val viewModel: MainViewModel by viewModels { mainViewModelFactory }

  private var currentNavController: LiveData<NavController>? = null
  private var bottomNavigationView: BottomNavigationView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (savedInstanceState == null) {
      setupBottomNavigationBar()
      addPromotionNotificationBadge()
    }
    viewModel.collectStateAndEvents(lifecycle, lifecycleScope)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    setupBottomNavigationBar()
    addPromotionNotificationBadge()
  }

  override fun onStateChanged(state: MainState) {
    setPromotionBadgeVisibility(state.showPromotionsBadge)
  }

  private fun setPromotionBadgeVisibility(showPromotionsBadge: Boolean) {
    promotionBadge?.visibility = if (showPromotionsBadge) View.VISIBLE else View.INVISIBLE
  }

  override fun onSideEffect(sideEffect: MainSideEffect) {
    when (sideEffect) {
      MainSideEffect.ShowPromotionsTooltip -> showPromotionsOverlay()
    }
  }

  private fun showPromotionsOverlay() {
    navigator.showPromotionsOverlay(bottomNavItems.indexOf(BottomNavItem.PROMOTIONS))
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
        intent = intent,
        onSelectedItem = { index ->
          if (index == bottomNavItems.indexOf(BottomNavItem.PROMOTIONS)) {
            viewModel.navigatedToPromotions()
          }
        }
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
      // Do nothing
    }
  }

  private fun setupTopUpItem(bottomNavigationView: BottomNavigationView?) {
    val menuView = bottomNavigationView?.getChildAt(0) as BottomNavigationMenuView
    val topUpItemView = menuView.getChildAt(bottomNavItems.size) as BottomNavigationItemView
    val topUpCustomView = LayoutInflater.from(this)
        .inflate(R.layout.bottom_nav_top_up_item, menuView, false)
    topUpCustomView.setOnClickListener {
      navigator.navigateToTopUp()
    }
    topUpItemView.addView(topUpCustomView)
  }

  private fun addPromotionNotificationBadge() {
    val menuView = bottomNavigationView?.getChildAt(0) as BottomNavigationMenuView
    val promotionsIcon = menuView.getChildAt(bottomNavItems.indexOf(BottomNavItem.PROMOTIONS))
    val itemView = promotionsIcon as BottomNavigationItemView
    promotionBadge = LayoutInflater.from(this)
        .inflate(R.layout.notification_badge, menuView, false)
    promotionBadge?.visibility = View.INVISIBLE
    itemView.addView(promotionBadge)
  }

  override fun onSupportNavigateUp(): Boolean {
    return currentNavController?.value?.navigateUp() ?: false
  }

  companion object {
    fun newIntent(context: Context, supportNotificationClicked: Boolean): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      }
    }
  }
}