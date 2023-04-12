package com.asfoundation.wallet.ui.overlay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
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

  private val binding by viewBinding(OverlayFragmentBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    handleItemAndArrowPosition()
    presenter.present()
  }

  private fun handleItemAndArrowPosition() {
    //Highlights the correct BN item
    val size = binding.overlayBottomNavigation.menu.size()
    for (i in 0 until size) {
      if (i != item) {
        binding.overlayBottomNavigation.menu.getItem(i)
            .icon = null
        binding.overlayBottomNavigation.menu.getItem(i)
            .title = ""
      }
    }
    //If selected view is not on the first half of the Bottom Navigation hide arrow
    if (item > size / 2) {
      binding.arrowDownTip.visibility = INVISIBLE
    } else {
      setArrowPosition()
    }

  }

  private fun setArrowPosition() {
    val bottomNavigationMenuView = (binding.overlayBottomNavigation as BottomNavigationView)
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
            binding.arrowDownTip.x =
                location[0] * 1f + (icon.width / 2f) - (binding.arrowDownTip.width / 2f)
          }
        })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.overlay_fragment, container, false)
  }

  override fun discoverClick(): Observable<Any> {
    return RxView.clicks(binding.discoverButton)
  }

  override fun dismissClick(): Observable<Any> {
    return RxView.clicks(binding.dismissButton)
  }

  override fun dismissView() {
    requireActivity().onBackPressed()
  }

  override fun overlayClick(): Observable<Any> {
    return RxView.clicks(binding.overlayContainer)
  }

  override fun navigateToPromotions() {
    navBarFragmentNavigator.navigateToPromotions()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
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
