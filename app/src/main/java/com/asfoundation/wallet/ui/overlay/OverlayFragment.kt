package com.asfoundation.wallet.ui.overlay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asf.wallet.databinding.OverlayFragmentBinding
import com.asfoundation.wallet.main.nav_bar.NavBarFragmentNavigator
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

@AndroidEntryPoint
class OverlayFragment : BasePageViewFragment(), OverlayView {

  @Inject
  lateinit var presenter: OverlayPresenter

  @Inject
  lateinit var navBarFragmentNavigator: NavBarFragmentNavigator

  private val item: Int by lazy {
    if (requireArguments().containsKey(ITEM_KEY)) {
      requireArguments().getInt(ITEM_KEY)
    } else {
      throw IllegalArgumentException("item not found")
    }
  }

  private var _binding: OverlayFragmentBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val overlay_bottom_navigation get() = binding.overlayBottomNavigation
  private val arrow_down_tip get() = binding.arrowDownTip
  private val discover_button get() = binding.discoverButton
  private val dismiss_button get() = binding.dismissButton
  private val overlay_container get() = binding.overlayContainer

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    handleItemAndArrowPosition()
    presenter.present()
  }

  private fun handleItemAndArrowPosition() {
    //Highlights the correct BN item
    val size = overlay_bottom_navigation.menu.size()
    for (i in 0 until size) {
      if (i != item) {
        overlay_bottom_navigation.menu.getItem(i)
            .icon = null
        overlay_bottom_navigation.menu.getItem(i)
            .title = ""
      }
    }
    //If selected view is not on the first half of the Bottom Navigation hide arrow
    if (item > size / 2) {
      arrow_down_tip.visibility = INVISIBLE
    } else {
      setArrowPosition()
    }

  }

  private fun setArrowPosition() {
    val bottomNavigationMenuView = (overlay_bottom_navigation as BottomNavigationView)
        .getChildAt(0) as BottomNavigationMenuView
    val promotionsIcon = bottomNavigationMenuView.getChildAt(item)
    val itemView = promotionsIcon as BottomNavigationItemView
    val icon = itemView.getChildAt(1)
    icon.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
          override fun onGlobalLayout() {
            icon.viewTreeObserver.removeOnGlobalLayoutListener(this)
            val location = IntArray(2)
            icon.getLocationInWindow(location)
            arrow_down_tip.x =
                location[0] * 1f + (icon.width / 2f) - (arrow_down_tip.width / 2f)
          }
        })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.overlay_fragment, container, false)
  }

  override fun discoverClick(): Observable<Any> {
    return RxView.clicks(discover_button)
  }

  override fun dismissClick(): Observable<Any> {
    return RxView.clicks(dismiss_button)
  }

  override fun dismissView() {
    requireActivity().onBackPressed()
  }

  override fun overlayClick(): Observable<Any> {
    return RxView.clicks(overlay_container)
  }

  override fun navigateToPromotions() {
    navBarFragmentNavigator.navigateToPromotions()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private const val ITEM_KEY = "item"

    @JvmStatic
    fun newInstance(highlightedBottomNavigationItem: Int): Fragment {
      val fragment = OverlayFragment()
      val bundle = Bundle()
      bundle.putInt(ITEM_KEY, highlightedBottomNavigationItem)
      fragment.arguments = bundle
      return fragment
    }
  }
}
