package com.asfoundation.wallet.promotions.voucher

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_app_bar.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.voucher_details_content_scrolling.*
import kotlinx.android.synthetic.main.voucher_details_download_app_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class VoucherDetailsFragment : DaggerFragment(), VoucherDetailsView {

  private lateinit var onBackPressedSubject: PublishSubject<Any>
  private lateinit var voucherSkuAdapter: VoucherSkuAdapter
  private lateinit var skuButtonClick: PublishSubject<Int>

  @Inject
  lateinit var presenter: VoucherDetailsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
    onBackPressedSubject = PublishSubject.create()
    skuButtonClick = PublishSubject.create()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return if (item.itemId == android.R.id.home) {
      onBackPressedSubject.onNext(Unit)
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_voucher_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUi(title: String, featureGraphic: String, icon: String, maxBonus: Double,
                       packageName: String, hasAppcoins: Boolean) {
    activity?.toolbar?.title = getString(R.string.voucher_buy_header, title)
    app_title.text = title
    if (maxBonus > 0.0) {
      setupBonusString(maxBonus)
    } else {
      app_bonus.visibility = View.GONE
    }
    appcoins_component.apply { visibility = if (hasAppcoins) View.VISIBLE else View.GONE }
    download_app_title.text = getString(R.string.voucher_buy_no_game_title, title)
    setupImages(featureGraphic, icon)
  }

  private fun setupBonusString(maxBonus: Double) {
    val formatter = CurrencyFormatUtils.create()
    val bonus = formatter.formatGamificationValues(BigDecimal(maxBonus))
    val prefix = getString(R.string.voucher_card_body_1)
    val suffix = getString(R.string.voucher_card_body_2, bonus)
    val spannable = SpannableString("$prefix $suffix")
    spannable.setSpan(StyleSpan(Typeface.BOLD), prefix.length, spannable.length - 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    app_bonus.text = spannable
  }

  private fun setupImages(featureGraphic: String, icon: String) {
    GlideApp.with(requireContext())
        .load(featureGraphic)
        .error(R.drawable.background_dark_grey)
        .into(app_featured_graphic)
    GlideApp.with(requireContext())
        .load(icon)
        .into(app_icon)
  }

  override fun setupSkus(voucherSkuItems: List<VoucherSkuItem>) {
    voucherSkuAdapter = VoucherSkuAdapter(voucherSkuItems, skuButtonClick)
    resources.apply {
      val verticalSize = getDimension(R.dimen.voucher_details_grid_view_vertical_spacing)
      val horizontalSize = getDimension(R.dimen.voucher_details_grid_view_horizontal_spacing)
      sku_recycler_view.addItemDecoration(
          MarginItemDecoration(horizontalSize.toInt(), verticalSize.toInt()))
    }
    sku_recycler_view.adapter = voucherSkuAdapter
  }

  override fun onNextClicks(): Observable<VoucherSkuItem> {
    return RxView.clicks(next_button)
        .map { voucherSkuAdapter.getSelectedSku() }
  }

  override fun onCancelClicks(): Observable<Any> = RxView.clicks(cancel_button)

  override fun onBackPressed(): Observable<Any> = onBackPressedSubject

  override fun onSkuButtonClick(): Observable<Int> = skuButtonClick

  override fun onRetryClick() = RxView.clicks(retry_button)

  override fun onDownloadButtonClick(): Observable<Any> = RxView.clicks(download_app_button)

  override fun showRetryAnimation() {
    retry_button.visibility = View.INVISIBLE
    retry_animation.visibility = View.VISIBLE
  }

  override fun showLoading() {
    loading_progress.visibility = View.VISIBLE
    no_network_layout.visibility = View.GONE
  }

  override fun hideLoading() {
    loading_progress.visibility = View.GONE
    no_network_layout.visibility = View.GONE
    main_content.visibility = View.VISIBLE
  }

  override fun showNoNetworkError() {
    retry_animation.visibility = View.GONE
    no_network_layout.visibility = View.VISIBLE
    retry_button.visibility = View.VISIBLE
    main_content.visibility = View.GONE
  }

  override fun setSelectedSku(index: Int) {
    voucherSkuAdapter.setSelectedSku(index)
    next_button.isEnabled = true
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  companion object {
    internal const val TITLE = "title"
    internal const val FEATURE_GRAPHIC = "feature_graphic"
    internal const val ICON = "icon"
    internal const val MAX_BONUS = "max_bonus"
    internal const val PACKAGE_NAME = "packageName"
    internal const val HAS_APPCOINS = "has_appcoins"

    @JvmStatic
    fun newInstance(title: String, featureGraphic: String, icon: String, maxBonus: Double,
                    packageName: String, hasAppcoins: Boolean): VoucherDetailsFragment {
      val fragment = VoucherDetailsFragment()
      fragment.arguments = Bundle().apply {
        putString(TITLE, title)
        putString(FEATURE_GRAPHIC, featureGraphic)
        putString(ICON, icon)
        putDouble(MAX_BONUS, maxBonus)
        putString(PACKAGE_NAME, packageName)
        putBoolean(HAS_APPCOINS, hasAppcoins)
      }
      return fragment
    }
  }
}
