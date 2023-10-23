package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asf.wallet.databinding.RewardPaymentLayoutBinding
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.util.TransferParser
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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

  private val binding by lazy { RewardPaymentLayoutBinding.bind(requireView()) }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = RewardPaymentLayoutBinding.inflate(inflater).root

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
    binding.genericErrorLayout.genericPurchaseErrorLayout.visibility = View.GONE
    binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted.visibility = View.INVISIBLE
    binding.loadingAnimation.visibility = View.VISIBLE
    binding.makingPurchaseText.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    binding.loadingAnimation.visibility = View.GONE
    binding.makingPurchaseText.visibility = View.GONE
  }

  override fun showNoNetworkError() {
    hideLoading()
    binding.genericErrorLayout.errorDismiss.setText(getString(R.string.ok))
    binding.genericErrorLayout.genericErrorLayout.errorMessage.setText(R.string.activity_iab_no_network_message)
    binding.genericErrorLayout.genericPurchaseErrorLayout.visibility = View.VISIBLE
  }

  override fun getOkErrorClick() = RxView.clicks(binding.genericErrorLayout.errorDismiss)

  override fun getSupportIconClick() = RxView.clicks(binding.genericErrorLayout.genericErrorLayout.layoutSupportIcn)

  override fun getSupportLogoClick() = RxView.clicks(binding.genericErrorLayout.genericErrorLayout.layoutSupportLogo)

  override fun close() = iabView.close(billingMessagesMapper.mapCancellation())

  override fun showError(message: Int?) {
    binding.genericErrorLayout.errorDismiss.setText(getString(R.string.back_button))
    binding.genericErrorLayout.genericErrorLayout.errorMessage.text = getString(message ?: R.string.activity_iab_error_message)
    binding.genericErrorLayout.genericPurchaseErrorLayout.visibility = View.VISIBLE
    hideLoading()
  }

  override fun finish(uid: String?, purchaseUid: String) {
    presenter.sendPaymentEvent()
    presenter.sendRevenueEvent()
    presenter.sendPaymentSuccessEvent(purchaseUid)
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
    presenter.sendPaymentSuccessEvent(purchase.uid)
    val bundle = billingMessagesMapper.mapPurchase(purchase, orderReference)
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
    iabView.finish(bundle)
  }

  override fun showVerification() = iabView.showVerification(false)

  override fun showTransactionCompleted() {
    binding.loadingAnimation.visibility = View.GONE
    binding.makingPurchaseText.visibility = View.GONE
    binding.genericErrorLayout.genericPurchaseErrorLayout.visibility = View.GONE
    binding.fragmentIabTransactionCompleted.iabActivityTransactionCompleted.visibility = View.VISIBLE
    binding.fragmentIabTransactionCompleted.bonusSuccessLayout.visibility = View.GONE
  }

  override fun getAnimationDuration() = binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.duration * 2

  override fun lockRotation() = iabView.lockRotation()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "AppcoinsRewardsBuyFragment must be attached to IAB activity" }
    iabView = context
  }

  private fun setupTransactionCompleteAnimation() =
    binding.fragmentIabTransactionCompleted.lottieTransactionSuccess.setAnimation(R.raw.success_animation)

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