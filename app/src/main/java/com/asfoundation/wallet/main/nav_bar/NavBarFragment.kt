package com.asfoundation.wallet.main.nav_bar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.common.createColoredString
import com.appcoins.wallet.ui.common.setTextFromColored
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon
import com.asf.wallet.R
import com.asf.wallet.databinding.NavBarFragmentBinding
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavBarFragment : BasePageViewFragment(),
  SingleStateFragment<NavBarState, NavBarSideEffect> {

  private lateinit var navHostFragment: NavHostFragment
  private lateinit var fullHostFragment: NavHostFragment

  private val views by viewBinding(NavBarFragmentBinding::bind)
  private val viewModel: NavBarViewModel by viewModels()

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
    setupTopUpItem()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    setBottomNavListener()
    setVipCalloutClickListener()
    views.composeView.setContent { MaterialTheme { BottomNavigationHome() } }
  }

  @Composable
  fun BottomNavigationHome() {
    BottomAppBar(
      containerColor = styleguide_blue_secondary,
      modifier = Modifier.height(64.dp),
      content = {
        Row(
          horizontalArrangement = Arrangement.SpaceEvenly,
          modifier = Modifier.fillMaxWidth()
        ) {

          viewModel.navigationItems().forEach { item ->
            val selected = viewModel.clickedItem.value == item.destination.ordinal
            ButtonWithIcon(
              icon = item.icon,
              label = item.label,
              backgroundColor = if (selected) styleguide_pink else styleguide_blue_secondary,
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
      }
    )
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
      Destinations.REWARDS -> navigator.navigateToMyWallets() //TODO Change it to Rewards
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
    setPromotionBadge(state.showPromotionsBadge)
    setVipCallout(state.shouldShowVipCallout)
  }

  override fun onSideEffect(sideEffect: NavBarSideEffect) {
    when (sideEffect) {
      NavBarSideEffect.ShowPromotionsTooltip -> showPromotionsOverlay()
      NavBarSideEffect.ShowOnboardingGPInstall -> showOnboardingIap()
      NavBarSideEffect.ShowOnboardingPendingPayment -> showOnboardingPayment()
    }
  }

  private fun setPromotionBadge(showPromotionsBadge: Boolean) {
    if (showPromotionsBadge) {
      views.bottomNav.getOrCreateBadge(R.id.promotions_graph).apply {
        backgroundColor = ContextCompat.getColor(requireContext(), R.color.styleguide_pink)
        isVisible = true
      }
    }
  }

  private fun setVipCallout(shouldShowVipCallout: Boolean) {
    if (shouldShowVipCallout)
      showVipCallout()
    else
      hideVipCallout()
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

  @SuppressLint("SetTextI18n", "ResourceType")
  private fun showVipCallout() {
    views.vipPromotionsCallout.vipCalloutLayout.visibility = View.VISIBLE
    val htmlColoredText =
      "${
        getString(R.string.vip_program_promotions_tab_1).createColoredString(
          getString(R.color.styleguide_vip_yellow)
        )
      } ${
        getString(R.string.vip_program_promotions_tab_2).createColoredString(
          getString(R.color.styleguide_white)
        )
      }"
    views.vipPromotionsCallout.desciptionTv.setTextFromColored(htmlColoredText)
  }

  private fun hideVipCallout() {
    views.vipPromotionsCallout.vipCalloutLayout.visibility = View.GONE
  }

  private fun setBottomNavListener() {
    views.bottomNav.setOnItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.home_graph -> {
        }
        R.id.promotions_graph -> {
          viewModel.removePromotionsBadge()
          if (views.vipPromotionsCallout.vipCalloutLayout.isVisible) {
            hideVipCallout()
            viewModel.vipPromotionsSeen()
          }
        }
        R.id.my_wallets_graph -> {
        }
        else -> {
        }
      }

      // proceed with the default navigation behaviour:
      onNavDestinationSelected(menuItem, navHostFragment.navController)
      return@setOnItemSelectedListener true
    }
  }

  private fun setVipCalloutClickListener() {
    views.vipPromotionsCallout.vipCalloutLayout.setOnClickListener {
      navBarAnalytics.sendCallOutEvent()
      val menuView = views.bottomNav.getChildAt(0) as BottomNavigationMenuView
      val promoItemView = menuView.getChildAt(1) as BottomNavigationItemView
      promoItemView.performClick()
    }
  }

  private fun setupTopUpItem() {
    val menuView = views.bottomNav.getChildAt(0) as BottomNavigationMenuView
    val topUpItemView = menuView.getChildAt(3) as BottomNavigationItemView
    val topUpCustomView = LayoutInflater.from(requireContext())
      .inflate(R.layout.bottom_nav_top_up_item, menuView, false)
    topUpCustomView.setOnClickListener {
      navigator.navigateToTopUp()
    }
    topUpItemView.addView(topUpCustomView)
  }
}

