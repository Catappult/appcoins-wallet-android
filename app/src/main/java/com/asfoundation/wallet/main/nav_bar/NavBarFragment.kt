package com.asfoundation.wallet.main.nav_bar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon
import com.appcoins.wallet.ui.widgets.expanded
import com.asf.wallet.R
import com.asf.wallet.databinding.NavBarFragmentBinding
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavBarFragment : BasePageViewFragment(),
  SingleStateFragment<NavBarState, NavBarSideEffect> {

  private lateinit var navHostFragment: NavHostFragment
  private lateinit var fullHostFragment: NavHostFragment

  private val views by viewBinding(NavBarFragmentBinding::bind)
  private val viewModel: NavBarViewModel by activityViewModels()

  @Inject
  lateinit var navigator: NavBarFragmentNavigator

  @Inject
  lateinit var navBarAnalytics: NavBarAnalytics

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = NavBarFragmentBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initHostFragments()
    views.bottomNav.setupWithNavController(navHostFragment.navController)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setBottomNavListener()
    views.composeView.setContent { MaterialTheme { BottomNavigationHome() } }
  }

  @Composable
  fun BottomNavigationHome() {
    BoxWithConstraints {
      if (expanded()) {
        Card(
          colors = CardDefaults.cardColors(containerColor = styleguide_blue),
          modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape)
        ) {
          Row(
            modifier = Modifier
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .background(shape = CircleShape, color = styleguide_blue)
          ) {
            NavigationItems(styleguide_blue)
          }
        }
      } else {
        BottomAppBar(
          containerColor = styleguide_blue_secondary,
          modifier = Modifier
            .height(64.dp),
          content = {
            Row(
              horizontalArrangement = Arrangement.SpaceEvenly,
              modifier = Modifier.fillMaxWidth()
            ) {
              NavigationItems(styleguide_blue_secondary)
            }
          }
        )
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
        }
      )
    }
  }

  @Preview
  @Composable
  fun PreviewBottomNavigationHome() {
    BottomNavigationHome()
  }

  override fun onResume() {
    super.onResume()
    viewModel.handleVipCallout()
  }

  private fun navigateToDestination(destinations: Destinations) {
    when (destinations) {
      Destinations.HOME -> navigator.navigateToHome()
      Destinations.REWARDS -> navigator.navigateToRewards()
    }
  }

  private fun initHostFragments() {
    navHostFragment = childFragmentManager.findFragmentById(
      R.id.nav_host_container
    ) as NavHostFragment
    fullHostFragment = childFragmentManager.findFragmentById(
      R.id.full_host_container
    ) as NavHostFragment
  }

  override fun onStateChanged(state: NavBarState) {
    //Do nothing
  }

  override fun onSideEffect(sideEffect: NavBarSideEffect) {
    when (sideEffect) {
      NavBarSideEffect.ShowPromotionsTooltip -> showPromotionsOverlay()
      NavBarSideEffect.ShowOnboardingGPInstall -> showOnboardingIap()
      NavBarSideEffect.ShowOnboardingPendingPayment -> showOnboardingPayment()
    }
  }

  private fun showPromotionsOverlay() {
    navigator.showPromotionsOverlay(requireActivity() as MainActivity, 1)
  }

  private fun showOnboardingIap() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingGPInstallScreen(fullHostFragment.navController)
  }

  private fun showOnboardingPayment() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingPaymentScreen(fullHostFragment.navController)
  }

  private fun setBottomNavListener() {
    views.bottomNav.setOnItemSelectedListener { menuItem ->
      // proceed with the default navigation behaviour:
      onNavDestinationSelected(menuItem, navHostFragment.navController)
      return@setOnItemSelectedListener true
    }
  }
}

