package com.asfoundation.wallet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.overlay_fragment.*


class OverlayFragment : Fragment(), OverlayView {

  private lateinit var presenter: OverlayPresenter
  private lateinit var activity: TransactionsActivity

  private val item: Int by lazy {
    if (arguments!!.containsKey(ITEM_KEY)) {
      arguments!!.getInt(ITEM_KEY)
    } else {
      throw IllegalArgumentException("item not found")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = OverlayPresenter(this, CompositeDisposable())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TransactionsActivity) {
      throw IllegalArgumentException(
          OverlayFragment::class.java.simpleName + " needs to be attached to a " + TransactionsActivity::class.java.simpleName)
    }
    activity = context
  }

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
      setArrowPosition(size)
    }

  }

  private fun setArrowPosition(size: Int) {
    val lp = bn_icon_guideline.layoutParams as ConstraintLayout.LayoutParams
    val itemPercentage = 1f / size.toFloat()
    val middleItemPercentage = itemPercentage / 2f
    val arrowPercentage = itemPercentage * (item.toFloat() + 1f) - middleItemPercentage
    lp.guidePercent = arrowPercentage
    bn_icon_guideline.layoutParams = lp
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
    activity.onBackPressed()
  }

  override fun overlayClick(): Observable<Any> {
    return RxView.clicks(overlay_container)
  }

  override fun navigateToPromotions() {
    activity.navigateToPromotions(true)
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
