package com.asfoundation.wallet.main.nav_bar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.NavBarFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.main.MainActivity
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

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.nav_bar_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initHostFragments()
    views.bottomNav.setupWithNavController(navHostFragment.navController)
    setupTopUpItem()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
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
  }

  override fun onSideEffect(sideEffect: NavBarSideEffect) {
    when (sideEffect) {
      NavBarSideEffect.ShowPromotionsTooltip -> showPromotionsOverlay()
      NavBarSideEffect.ShowOnboardingIap -> showOnboardingIap()
    }
  }

  private fun setPromotionBadge(showPromotionsBadge: Boolean) {
    if (showPromotionsBadge) {
      views.bottomNav.getOrCreateBadge(R.id.promotions_graph).apply {
        backgroundColor = ContextCompat.getColor(requireContext(), R.color.wild_watermelon)
        isVisible = true
      }
    }
  }

  private fun showPromotionsOverlay() {
    navigator.showPromotionsOverlay(requireActivity() as MainActivity, 1)
  }

  private fun showOnboardingIap() {
    views.fullHostContainer.visibility = View.VISIBLE
    navigator.showOnboardingIapScreen(fullHostFragment.navController)
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

