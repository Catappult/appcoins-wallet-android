package com.asfoundation.wallet.promotions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_promotions.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

class PromotionsFragment : BasePageViewFragment(), PromotionsView {

  @Inject
  lateinit var gamification: GamificationInteractor

  @Inject
  lateinit var promotionsInteractor: PromotionsInteractorContract

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mapper: GamificationMapper

  private lateinit var activityView: PromotionsActivityView
  private lateinit var presenter: PromotionsFragmentPresenter
  private lateinit var adapter: PromotionsAdapter
  private var clickListener: PublishSubject<String>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickListener = PublishSubject.create()
    presenter =
        PromotionsFragmentPresenter(this, activityView, promotionsInteractor, CompositeDisposable(),
            Schedulers.io(),
            AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is PromotionsActivityView) { PromotionsFragment::class.java.simpleName + " needs to be attached to a " + PromotionsActivityView::class.java.simpleName }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun showPromotions(promotionsModel: PromotionsModel) {
    adapter = PromotionsAdapter(promotionsModel.promotions, clickListener!!)
    rv_promotions.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.promotions_item_margin)
            .toInt()))
    rv_promotions.visibility = VISIBLE
    rv_promotions.adapter = adapter
  }

  override fun showLoading() {
    promotions_progress_bar.visibility = VISIBLE
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getPromotionClicks() = clickListener!!

  override fun showNetworkErrorView() {
    no_promotions.visibility = GONE
    no_network.visibility = VISIBLE
    retry_button.visibility = VISIBLE
    retry_animation.visibility = GONE
  }

  override fun showNoPromotionsScreen() {
    no_network.visibility = GONE
    retry_animation.visibility = GONE
    no_promotions.visibility = VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = INVISIBLE
    retry_animation.visibility = VISIBLE
  }

  override fun hideLoading() {
    promotions_progress_bar.visibility = INVISIBLE
  }

  override fun hidePromotions() {
    rv_promotions.visibility = GONE
  }

  override fun onResume() {
    presenter.present()
    super.onResume()
  }

  override fun onPause() {
    presenter.stop()
    super.onPause()
  }

  companion object {
    fun newInstance() = PromotionsFragment()
  }
}
