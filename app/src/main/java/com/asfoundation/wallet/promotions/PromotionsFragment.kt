package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_promotions.*
import kotlinx.android.synthetic.main.gamification_info_bottom_sheet.*
import kotlinx.android.synthetic.main.layout_app_bar.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

class PromotionsFragment : BasePageViewFragment(), PromotionsView {

  @Inject
  lateinit var presenter: PromotionsPresenter

  private lateinit var adapter: PromotionsAdapter
  private lateinit var detailsBottomSheet: BottomSheetBehavior<View>
  private lateinit var transactionsRouter: TransactionsRouter
  private var clickListener: PublishSubject<PromotionClick>? = null
  private var onBackPressedSubject: PublishSubject<Any>? = null
  private var backEnabled = true

  companion object {
    fun newInstance() = PromotionsFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    transactionsRouter = TransactionsRouter()
    clickListener = PublishSubject.create()
    onBackPressedSubject = PublishSubject.create()
  }

  private fun toolbar(): Toolbar? {
    if (toolbar != null) {
      (activity as AppCompatActivity).setSupportActionBar(toolbar)
      toolbar.title = (activity as AppCompatActivity).title
    }
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    return toolbar
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar()
    adapter = PromotionsAdapter(emptyList(), clickListener!!)
    rv_promotions.adapter = adapter
    rv_promotions.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.promotions_item_margin)
            .toInt()))
    detailsBottomSheet = BottomSheetBehavior.from(bottom_sheet_fragment_container)
    detailsBottomSheet.addBottomSheetCallback(
        object : BottomSheetBehavior.BottomSheetCallback() {
          override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

          override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 0f) bottomsheet_coordinator_container.visibility = GONE
            bottomsheet_coordinator_container.background.alpha = (255 * slideOffset).toInt()
          }
        })
    presenter.present()
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun showPromotions(promotionsModel: PromotionsModel) {
    adapter.setPromotions(promotionsModel.promotions)
    rv_promotions.visibility = VISIBLE
    no_network.visibility = GONE
    locked_promotions.visibility = GONE
    no_promotions.visibility = GONE
  }

  override fun showLoading() {
    promotions_progress_bar.visibility = VISIBLE
    locked_promotions.visibility = GONE
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getPromotionClicks() = clickListener!!

  override fun getGamificationInfoClicks(): Observable<Unit> = RxView.clicks(gamification_info_btn)
      .map { }

  override fun showNetworkErrorView() {
    rv_promotions.visibility = GONE
    no_promotions.visibility = GONE
    no_network.visibility = VISIBLE
    retry_button.visibility = VISIBLE
    retry_animation.visibility = GONE
  }

  override fun showNoPromotionsScreen() {
    no_network.visibility = GONE
    retry_animation.visibility = GONE
    no_promotions.visibility = VISIBLE
    locked_promotions.visibility = GONE
  }

  override fun showLockedPromotionsScreen() {
    no_network.visibility = GONE
    retry_animation.visibility = GONE
    no_promotions.visibility = GONE
    locked_promotions.visibility = VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = INVISIBLE
    retry_animation.visibility = VISIBLE
  }

  override fun hideLoading() {
    promotions_progress_bar.visibility = INVISIBLE
  }

  override fun handleBackPressed() {
    // Currently we only call the hide bottom sheet
    // but maybe later additional stuff needs to be handled
    hideBottomSheet()
  }

  override fun getBottomSheetButtonClick() = RxView.clicks(got_it_button)

  override fun getBackPressed() = onBackPressedSubject!!

  override fun hideBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    disableBackListener(bottomsheet_coordinator_container)
  }

  override fun showBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    bottomsheet_coordinator_container.visibility = VISIBLE
    bottomsheet_coordinator_container.background.alpha = 255
    setBackListener(bottomsheet_coordinator_container)
  }

  override fun getBottomSheetContainerClick() = RxView.clicks(bottomsheet_coordinator_container)

  override fun showToast() {
    Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
      if (backEnabled) {
        activity?.finish()
      } else {
        onBackPressedSubject?.onNext(Unit)
      }
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  private fun setBackListener(view: View) {
    backEnabled = false
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
            onBackPressedSubject?.onNext(Unit)
        }
        true
      }
    }
  }

  private fun disableBackListener(view: View) {
    backEnabled = true
    view.apply {
      isFocusableInTouchMode = false
      setOnKeyListener(null)
    }
  }
}
