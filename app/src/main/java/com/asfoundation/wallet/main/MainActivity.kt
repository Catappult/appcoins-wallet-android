package com.asfoundation.wallet.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityMainBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.main.appsflyer.ApkOriginVerification
import com.asfoundation.wallet.navigator.setupWithNavController
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingFromIapUseCase
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SingleStateFragment<MainState, MainSideEffect> {

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

  //TODO remove usecase usage, only for testing
  @Inject
  lateinit var setOnboardingFromIapUseCase: SetOnboardingFromIapUseCase

  private val views by viewBinding(ActivityMainBinding::bind)

  private val viewModel: MainViewModel by viewModels()

  private var currentNavController: LiveData<NavController>? = null

  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.MaterialAppTheme)
    super.onCreate(savedInstanceState)

    viewModel.handleInitialNavigation()
    setContentView(R.layout.activity_main)
    views.root.background =
      ContextCompat.getDrawable(this, R.drawable.splash_background)

    if (savedInstanceState == null) {
      appsflyerFirstRun()
    }

    //TODO remove usecase usage, only for testing
    setOnboardingFromIapUseCase()

    handleAuthenticationResult()
    viewModel.collectStateAndEvents(lifecycle, lifecycleScope)
  }

  private fun handleAuthenticationResult() {
    authenticationResultLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AuthenticationPromptActivity.RESULT_OK) {
          viewModel.handleInitialNavigation(authComplete = true)
        } else {
          finish()
        }
      }
  }

  private fun appsflyerFirstRun() {
    val isFirstRun = getSharedPreferences("PREFERENCE", 0)
      .getBoolean("isFirstRun", true)
    if (isFirstRun) {
      ApkOriginVerification(this)
      getSharedPreferences("PREFERENCE", 0)
        .edit()
        .putBoolean("isFirstRun", false)
        .apply()
    }
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
      MainSideEffect.NavigateToAutoUpdate -> navigator.navigateToAutoUpdate(this)
      MainSideEffect.NavigateToFingerprintAuthentication ->
        navigator.showAuthenticationActivity(authenticationResultLauncher)
      MainSideEffect.NavigateToOnboarding -> navigator.navigateToOnboarding(this, fromIap = false)
      MainSideEffect.NavigateToOnboardingIap -> {
        navigator.navigateToOnboarding(this, fromIap = true)
      }
      MainSideEffect.NavigateToHome -> showHomeContent()
    }
  }

  private fun showHomeContent() {
    views.root.background =
      ContextCompat.getDrawable(this, R.color.white)
    setupBottomNavigationBar()
    addPromotionNotificationBadge()
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

    views.bottomNav.visibility = View.VISIBLE

    val navGraphIds = bottomNavItems.map { it.navGraphIdRes }

    val controller = views.bottomNav.setupWithNavController(
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
    setupTopUpItem(views.bottomNav)

    currentNavController = controller
  }

  fun setSelectedBottomNavItem(bottomNavItem: BottomNavItem) {
    try {
      val index = bottomNavItems.indexOf(bottomNavItem)
      val menuItemId = views.bottomNav.menu[index].itemId
      menuItemId.let { views.bottomNav.selectedItemId = it }
    } catch (e: Exception) {
      Log.e(TAG, "Couldn't select nav item. " + e.message)
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
    val menuView = views.bottomNav.getChildAt(0) as BottomNavigationMenuView
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
    private const val TAG = "MainActivity"

    fun newIntent(context: Context, supportNotificationClicked: Boolean): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      }
    }
  }
}