package com.asfoundation.wallet.main.nav_bar

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.NetworkMonitor
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
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

  private lateinit var navHostFragment: NavHostFragment
  private lateinit var fullHostFragment: NavHostFragment
  private lateinit var mainHostFragment: NavHostFragment

  private val views by viewBinding(NavBarFragmentBinding::bind)
  private val viewModel: NavBarViewModel by activityViewModels()

  private val pushNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

  @Inject
  lateinit var navigator: NavBarFragmentNavigator

  @Inject
  lateinit var navBarAnalytics: NavBarAnalytics

  @Inject
  lateinit var networkMonitor: NetworkMonitor

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
//    adjustBottomNavigationViewOnKeyboardVisibility()  //TODO fix bottom nav
    setBottomNavListener()
    views.composeView.setContent { BottomNavigationHome() }
  }

  @Composable
  fun BottomNavigationHome() {
    val connectionObserver = networkMonitor.isConnected.collectAsState(true).value
    BoxWithConstraints(contentAlignment = Alignment.BottomEnd) {
      if (expanded()) {
        ConnectionAlert(isConnected = connectionObserver)
        Card(
          colors = CardDefaults.cardColors(containerColor = styleguide_blue),
          modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape)
        ) {
          Row(
            modifier =
            Modifier
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .background(shape = CircleShape, color = styleguide_blue)
          ) {
            NavigationItems(styleguide_blue)
          }
        }
      } else {
        Column(modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 60.dp, max = 64.dp)) {
          ConnectionAlert(isConnected = connectionObserver)
          Card(
            colors = CardDefaults.cardColors(containerColor = styleguide_blue),
            modifier = Modifier
              .padding(16.dp)
              .clip(CircleShape)
          ) {
            Row(
              horizontalArrangement = Arrangement.SpaceEvenly,
              modifier = Modifier
                .fillMaxSize()
                .heightIn(min = 60.dp, max = 64.dp)
            ) {
              NavigationItems(styleguide_blue_secondary)
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
      ButtonWithIcon(
        icon = item.icon,
        label = item.label,
        backgroundColor = if (selected) styleguide_pink else background,
        labelColor = if (selected) styleguide_white else styleguide_medium_grey,
        iconColor = if (selected) styleguide_white else styleguide_medium_grey,
        iconSize = 24.dp,
        onClick = {
          viewModel.clickedItem.value = item.destination.ordinal
          navigateToDestination(item.destination)
        })
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
      Destinations.HOME -> navigator.navigateToHome()
      Destinations.REWARDS -> navigator.navigateToRewards()
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
        showOnboardingRecoverGuestWallet(sideEffect.backup)

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

  private fun showOnboardingRecoverGuestWallet(backup: String) {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingRecoverGuestWallet(mainHostFragment.navController, backup)
  }

  private fun adjustBottomNavigationViewOnKeyboardVisibility() {
    views.root.viewTreeObserver?.addOnGlobalLayoutListener {
      val rect = Rect()
      views.root.getWindowVisibleDisplayFrame(rect)
      val screenHeight = views.root.height
      val keypadHeight = screenHeight - rect.bottom
      if (keypadHeight > screenHeight * 0.15) {
        //hide compose
        views.composeView.visibility = View.GONE
      } else {
        // show bottomNav
        views.composeView.visibility = View.VISIBLE
      }
    }
  }
}
