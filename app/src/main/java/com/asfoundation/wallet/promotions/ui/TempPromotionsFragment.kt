package com.asfoundation.wallet.promotions.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.PerksVouchersPageAdapter
import com.asfoundation.wallet.promotions.PerksVouchersPageChangeListener
import com.asfoundation.wallet.promotions.PerksVouchersViewHolder
import com.asfoundation.wallet.promotions.UniquePromotionsAdapter
import com.asfoundation.wallet.promotions.model.PromotionClick
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.util.addBottomItemDecoration
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_promotions.*
import kotlinx.android.synthetic.main.gamification_info_bottom_sheet.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.perks_and_vouchers_layout.*
import javax.inject.Inject


class TempPromotionsFragment : BasePageViewFragment(), PromotionsView {

  @Inject
  lateinit var presenter: TempPromotionsPresenter

  private lateinit var uniquePromotionsAdapter: UniquePromotionsAdapter
  private lateinit var perksVouchersPageAdapter: PerksVouchersPageAdapter
  private lateinit var detailsBottomSheet: BottomSheetBehavior<View>
  private lateinit var transactionsRouter: TransactionsRouter
  private lateinit var clickListener: PublishSubject<PromotionClick>
  private lateinit var onBackPressedSubject: PublishSubject<Any>
  private lateinit var pageChangedSubject: PublishSubject<Int>
  private var backEnabled = true

  companion object {
    fun newInstance() = TempPromotionsFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    transactionsRouter = TransactionsRouter()
    clickListener = PublishSubject.create()
    onBackPressedSubject = PublishSubject.create()
    pageChangedSubject = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setAdapters()
    createBottomSheet()
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
    perksVouchersPageAdapter.setItems(listOf(promotionsModel.vouchers, promotionsModel.perks))
    if (promotionsModel.vouchers.isEmpty() && promotionsModel.perks.isNotEmpty()) checkPerksRadioButton()
    uniquePromotionsAdapter.setPromotions(promotionsModel.promotions)
    rv_promotions.visibility = View.VISIBLE
    no_network.visibility = View.GONE
    locked_promotions.visibility = View.GONE
    no_promotions.visibility = View.GONE
    perks_vouchers_buttons.visibility = View.VISIBLE
  }

  override fun showLoading() {
    promotions_progress_bar.visibility = View.VISIBLE
    locked_promotions.visibility = View.GONE
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getPromotionClicks() = clickListener

  override fun showNetworkErrorView() {
    rv_promotions.visibility = View.GONE
    no_promotions.visibility = View.GONE
    no_network.visibility = View.VISIBLE
    retry_button.visibility = View.VISIBLE
    retry_animation.visibility = View.GONE
  }

  override fun showNoPromotionsScreen() {
    no_network.visibility = View.GONE
    retry_animation.visibility = View.GONE
    no_promotions.visibility = View.VISIBLE
    locked_promotions.visibility = View.GONE
  }

  override fun showLockedPromotionsScreen() {
    no_network.visibility = View.GONE
    retry_animation.visibility = View.GONE
    no_promotions.visibility = View.GONE
    locked_promotions.visibility = View.VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    promotions_progress_bar.visibility = View.INVISIBLE
  }

  override fun handleBackPressed() {
    // Currently we only call the hide bottom sheet
    // but maybe later additional stuff needs to be handled
    hideBottomSheet()
  }

  override fun getBottomSheetButtonClick() = RxView.clicks(got_it_button)

  override fun getBackPressed() = onBackPressedSubject

  override fun hideBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    disableBackListener(bottomsheet_coordinator_container)
  }

  override fun showBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    bottomsheet_coordinator_container.visibility = View.VISIBLE
    bottomsheet_coordinator_container.background.alpha = 255
    setBackListener(bottomsheet_coordinator_container)
  }

  override fun getVouchersRadioButtonClick(): Observable<Boolean> {
    return RxView.clicks(vouchers_button)
        .map { vouchers_button.isChecked }
  }

  override fun getPerksRadioButtonClick(): Observable<Boolean> {
    return RxView.clicks(perks_button)
        .map { perks_button.isChecked }
  }

  override fun pageChangedCallback() = pageChangedSubject

  override fun changeButtonState(position: Int) {
    if (position == PerksVouchersViewHolder.VOUCHER_POSITION) {
      perks_button.isChecked = false
      vouchers_button.isChecked = true
    } else {
      vouchers_button.isChecked = false
      perks_button.isChecked = true
    }
  }

  override fun checkVouchersRadioButton() {
    perks_button.isChecked = false
    vouchers_perks_viewpager.currentItem = PerksVouchersViewHolder.VOUCHER_POSITION
  }

  override fun checkPerksRadioButton() {
    vouchers_button.isChecked = false
    vouchers_perks_viewpager.currentItem = PerksVouchersViewHolder.PERKS_POSITION
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
        onBackPressedSubject.onNext(Unit)
      }
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  private fun createBottomSheet() {
    detailsBottomSheet = BottomSheetBehavior.from(bottom_sheet_fragment_container)
    detailsBottomSheet.addBottomSheetCallback(
        object : BottomSheetBehavior.BottomSheetCallback() {
          override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

          override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 0f) bottomsheet_coordinator_container.visibility = View.GONE
            bottomsheet_coordinator_container.background.alpha = (255 * slideOffset).toInt()
          }
        })
  }

  private fun setAdapters() {
    uniquePromotionsAdapter = UniquePromotionsAdapter(emptyList(), clickListener)
    perksVouchersPageAdapter = PerksVouchersPageAdapter(emptyList(), clickListener)
    rv_promotions.adapter = uniquePromotionsAdapter
    rv_promotions.addBottomItemDecoration(resources.getDimension(R.dimen.promotions_item_margin))
    vouchers_perks_viewpager.adapter = perksVouchersPageAdapter
    vouchers_perks_viewpager.registerOnPageChangeCallback(
        PerksVouchersPageChangeListener(pageChangedSubject))
  }


  private fun setBackListener(view: View) {
    backEnabled = false
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
            onBackPressedSubject.onNext(Unit)
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