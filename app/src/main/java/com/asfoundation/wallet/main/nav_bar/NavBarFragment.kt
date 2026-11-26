package com.asfoundation.wallet.main.nav_bar

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.NetworkMonitor
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_variant
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_primary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.NoNetworkSnackBar
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon
import com.appcoins.wallet.ui.widgets.expanded
import com.asf.wallet.R
import com.asf.wallet.databinding.NavBarFragmentBinding
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NavBarFragment : BasePageViewFragment(), SingleStateFragment<NavBarState, NavBarSideEffect> {

  companion object {
    const val EXTRA_GIFT_CARD = "giftCard"
    const val EXTRA_PROMO_CODE = "promoCode"
  }

  private lateinit var navHostFragment: NavHostFragment
  private lateinit var fullHostFragment: NavHostFragment
  private lateinit var mainHostFragment: NavHostFragment

  private val views by viewBinding(NavBarFragmentBinding::bind)
  private val viewModel: NavBarViewModel by activityViewModels()

  private val pushNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

  private var keyboardListener: OnGlobalLayoutListener? = null

  @Inject
  lateinit var navigator: NavBarFragmentNavigator

  @Inject
  lateinit var navBarAnalytics: NavBarAnalytics

  @Inject
  lateinit var networkMonitor: NetworkMonitor

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = NavBarFragmentBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initHostFragments()
    views.bottomNav.setupWithNavController(navHostFragment.navController)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    adjustBottomNavigationViewOnKeyboardVisibility()
    setBottomNavListener()
    views.composeView.setContent { BottomNavigationHome() }
    arguments?.getString(EXTRA_GIFT_CARD)?.let {
      handleGiftCard(it)
      arguments?.remove(EXTRA_GIFT_CARD)
    }
    arguments?.getString(EXTRA_PROMO_CODE)?.let {
      handlePromoCode(it)
      arguments?.remove(EXTRA_PROMO_CODE)
    }
  }

  fun handleGiftCard(giftCard: String) {
    viewModel.clickedItem.value = 1
    navigator.navigateToRewards(navHostFragment.navController, giftCard)
  }

  fun handlePromoCode(promoCode: String) {
    viewModel.clickedItem.value = 1
    navigator.navigateToRewards(navHostFragment.navController, promoCode = promoCode)
  }

  @Composable
  fun BottomNavigationHome() {
    val connectionObserver = networkMonitor.isConnected.collectAsState(true).value
    BoxWithConstraints(contentAlignment = Alignment.BottomEnd) {
      if (expanded()) {
        ConnectionAlert(isConnected = connectionObserver)
        Card(
          colors = CardDefaults.cardColors(containerColor = styleguide_dark),
          modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape)
        ) {
          Row(
            modifier =
            Modifier
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .background(shape = CircleShape, color = styleguide_dark)
          ) {
            NavigationItems(styleguide_dark)
          }
        }
      } else {
        Column(modifier = Modifier.fillMaxWidth()) {
          ConnectionAlert(isConnected = connectionObserver)
          // Floating pill, centred in the same strip the old full-width bar used.
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(64.dp)
              .background(styleguide_dark, RectangleShape),
            contentAlignment = Alignment.Center
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(CircleShape)
                .background(styleguide_dark_secondary)
                .border(1.dp, styleguide_dark_variant, CircleShape)
                // Buttons already carry a 4dp vertical touch-target inset, so 8dp sideways
                // keeps the gap even all round and the inner and outer curves concentric.
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              NavigationItems(styleguide_dark_secondary)
            }
          }
        }
      }
    }
  }

  @Composable
  fun NavigationItems(background: Color) {
    viewModel.navigationItems().forEach { item ->
      val selected = viewModel.clickedItem.value == item.destination.ordinal
      val itemBackground by animateColorAsState(
        if (selected) styleguide_primary else background,
        label = "navItemBackground"
      )
      ButtonWithIcon(
        icon = item.icon,
        label = item.label,
        backgroundColor = itemBackground,
        labelColor = if (selected) styleguide_white else styleguide_medium_grey,
        iconColor = if (selected) styleguide_white else styleguide_medium_grey,
        iconSize = 24.dp,
        onClick = {
          viewModel.clickedItem.value = item.destination.ordinal
          navigateToDestination(item.destination)
        },
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  @Composable
  fun ConnectionAlert(isConnected: Boolean) {
    if (!isConnected) NoNetworkSnackBar()
  }

  @Preview
  @Composable
  fun PreviewBottomNavigationHome() {
    BottomNavigationHome()
  }

  private fun navigateToDestination(destinations: Destinations) {
    when (destinations) {
      Destinations.HOME -> navigator.navigateToHome(navHostFragment.navController)
      Destinations.REWARDS -> navigator.navigateToRewards(navHostFragment.navController)
    }
  }

  private fun initHostFragments() {
    navHostFragment =
      childFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
    fullHostFragment =
      childFragmentManager.findFragmentById(R.id.full_host_container) as NavHostFragment
    mainHostFragment =
      activity?.supportFragmentManager?.findFragmentById(R.id.main_host_container)
          as NavHostFragment
  }

  override fun onStateChanged(state: NavBarState) {
    // DO NOTHING
  }

  override fun onSideEffect(sideEffect: NavBarSideEffect) {
    when (sideEffect) {
      NavBarSideEffect.ShowOnboardingGPInstall -> showOnboardingIap()
      NavBarSideEffect.ShowOnboardingPendingPayment -> showOnboardingPayment()
      is NavBarSideEffect.ShowOnboardingRecoverGuestWallet ->
        showOnboardingRecoverGuestWallet()

      NavBarSideEffect.ShowAskNotificationPermission -> askNotificationsPermission()
    }
  }

  private fun askNotificationsPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Android OS manages when to ask for permission. After android 11, the default behavior is
      // to only prompt for permission if needed, and if the user didn't deny it twice before. If
      // the user just dismissed it without denying, then it'll prompt again next time.
      pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  private fun setBottomNavListener() {
    views.bottomNav.setOnItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.home_graph -> {}
        R.id.reward_graph -> {}
      }

      // proceed with the default navigation behaviour:
      onNavDestinationSelected(menuItem, navHostFragment.navController)
      return@setOnItemSelectedListener true
    }
  }

  private fun showOnboardingIap() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingGPInstallScreen(fullHostFragment.navController)
  }

  private fun showOnboardingPayment() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingPaymentScreen(fullHostFragment.navController)
  }

  private fun showOnboardingRecoverGuestWallet() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingRecoverGuestWallet(
      navController = mainHostFragment.navController,
      createWalletAutomatically = false
    )
  }

  private fun adjustBottomNavigationViewOnKeyboardVisibility() {
    keyboardListener = OnGlobalLayoutListener {
      try {
        val rect = Rect()
        views.root.getWindowVisibleDisplayFrame(rect)
        val screenHeight = views.root.height
        val keypadHeight = screenHeight - rect.bottom
        if (keypadHeight > screenHeight * 0.15) {
          views.composeView.visibility = View.GONE
        } else {
          views.composeView.visibility = View.VISIBLE
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    views.root.viewTreeObserver?.addOnGlobalLayoutListener(keyboardListener)
  }

}
