package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.iab_error_layout.generic_error_layout
import kotlinx.android.synthetic.main.reward_payment_layout.*
import kotlinx.android.synthetic.main.support_error_layout.*
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class AppcoinsRewardsBuyFragment : BasePageViewFragment(), AppcoinsRewardsBuyView {

  @Inject
  lateinit var rewardsManager: RewardsManager

  @Inject
  lateinit var transferParser: TransferParser

  @Inject
  lateinit var billingMessagesMapper: BillingMessagesMapper

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var paymentAnalytics: PaymentMethodsAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var appcoinsRewardsBuyInteract: AppcoinsRewardsBuyInteract

  @Inject
  lateinit var logger: Logger

  private lateinit var presenter: AppcoinsRewardsBuyPresenter
  private lateinit var iabView: IabView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.reward_payment_layout, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter = AppcoinsRewardsBuyPresenter(
      view = this,
      rewardsManager = rewardsManager,
      viewScheduler = AndroidSchedulers.mainThread(),
      networkScheduler = Schedulers.io(),
      disposables = CompositeDisposable(),
      packageName = transactionBuilder.domain,
      isBds = isBds,
      isPreSelected = isPreSelected,
      analytics = analytics,
      paymentAnalytics = paymentAnalytics,
      transactionBuilder = transactionBuilder,
      formatter = formatter,
      gamificationLevel = gamificationLevel,
      appcoinsRewardsBuyInteract = appcoinsRewardsBuyInteract,
      logger = logger
    )
    setupTransactionCompleteAnimation()
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun finish(purchase: Purchase) = finish(purchase, null)

  override fun showLoading() {
    generic_error_layout.visibility = View.GONE
    iab_activity_transaction_completed.visibility = View.INVISIBLE
    loading_view.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    loading_view.visibility = View.GONE
  }

  override fun showNoNetworkError() {
    hideLoading()
    error_dismiss.setText(getString(R.string.ok))
    error_message.setText(R.string.activity_iab_no_network_message)
    generic_error_layout.visibility = View.VISIBLE
  }

  override fun getOkErrorClick() = RxView.clicks(error_dismiss)

  override fun getSupportIconClick() = RxView.clicks(layout_support_icn)

  override fun getSupportLogoClick() = RxView.clicks(layout_support_logo)

  override fun close() = iabView.close(billingMessagesMapper.mapCancellation())

  override fun showError(message: Int?) {
    error_dismiss.setText(getString(R.string.back_button))
    error_message.text = getString(message ?: R.string.activity_iab_error_message)
    generic_error_layout.visibility = View.VISIBLE
    hideLoading()
  }

  override fun finish(uid: String?) {
    presenter.sendPaymentEvent()
    presenter.sendRevenueEvent()
    presenter.sendPaymentSuccessEvent()
    val bundle = billingMessagesMapper.successBundle(uid)
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
    iabView.finish(bundle)
  }

  override fun errorClose() = iabView.close(billingMessagesMapper.genericError())

  override fun showPaymentMethods() {
    iabView.unlockRotation()
    iabView.showPaymentMethodsView()
  }

  override fun finish(purchase: Purchase, orderReference: String?) {
    presenter.sendPaymentEvent()
    presenter.sendRevenueEvent()
    presenter.sendPaymentSuccessEvent()
    val bundle = billingMessagesMapper.mapPurchase(purchase, orderReference)
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
    iabView.finish(bundle)
  }

  override fun showVerification() = iabView.showVerification(false)

  override fun showTransactionCompleted() {
    loading_view.visibility = View.GONE
    generic_error_layout.visibility = View.GONE
    iab_activity_transaction_completed.visibility = View.VISIBLE
  }

  override fun getAnimationDuration() = lottie_transaction_success.duration

  override fun lockRotation() = iabView.lockRotation()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "AppcoinsRewardsBuyFragment must be attached to IAB activity" }
    iabView = context
  }

  private fun setupTransactionCompleteAnimation() =
    lottie_transaction_success.setAnimation(R.raw.success_animation)

  private val isBds: Boolean by lazy {
    if (requireArguments().containsKey(IS_BDS)) {
      requireArguments().getBoolean(IS_BDS)
    } else {
      throw IllegalArgumentException("isBds not found")
    }
  }

  private val isPreSelected: Boolean by lazy {
    if (requireArguments().containsKey(PRE_SELECTED_KEY)) {
      requireArguments().getBoolean(PRE_SELECTED_KEY)
    } else {
      throw IllegalArgumentException("pre selected data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_KEY)) {
      requireArguments().getParcelable<TransactionBuilder>(TRANSACTION_KEY)!!
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  companion object {
    private const val AMOUNT_KEY = "amount"
    private const val URI_KEY = "uri_key"
    private const val IS_BDS = "is_bds"
    private const val TRANSACTION_KEY = "transaction_key"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val PRE_SELECTED_KEY = "pre_selected"

    fun newInstance(
      amount: BigDecimal,
      transactionBuilder: TransactionBuilder,
      uri: String?,
      isBds: Boolean,
      isPreSelected: Boolean,
      gamificationLevel: Int
    ): Fragment = AppcoinsRewardsBuyFragment().apply {
      arguments = Bundle().apply {
        putSerializable(AMOUNT_KEY, amount)
        putParcelable(TRANSACTION_KEY, transactionBuilder)
        putString(URI_KEY, uri)
        putBoolean(IS_BDS, isBds)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
      }
    }
  }
}