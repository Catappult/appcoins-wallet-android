package com.asfoundation.wallet.verification.ui.credit_card.intro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.util.AdyenCardView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.FragmentVerificationIntroBinding
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@AndroidEntryPoint
class VerificationIntroFragment : BasePageViewFragment(), VerificationIntroView {

  companion object {

    @JvmStatic
    fun newInstance() = VerificationIntroFragment()
  }

  @Inject
  lateinit var presenter: VerificationIntroPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var adyenEnvironment: Environment

  private lateinit var activityView: VerificationCreditCardActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var adyenCardView: AdyenCardView

  private var isStored = false
  private var paymentDataSubject: BehaviorSubject<AdyenCardWrapper> = BehaviorSubject.create()

  private val views by viewBinding(FragmentVerificationIntroBinding::bind)

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is VerificationCreditCardActivityView) {
      throw IllegalStateException(
        "Wallet Verification Intro must be attached to Wallet Verification Activity"
      )
    }
    activityView = context
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_verification_intro, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSavedInstance(outState)
  }

  private fun setupUi() {
    adyenCardView = AdyenCardView(views.adyenCardForm.root)
    setupCardConfiguration()
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration
      .Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()
  }

  @SuppressLint("StringFormatInvalid")
  override fun updateUi(verificationIntroModel: VerificationIntroModel) {
    val amount = formatter.formatCurrency(
      verificationIntroModel.verificationInfoModel.value,
      WalletCurrency.FIAT
    )
    views.description.text = getString(
      R.string.card_verification_charde_disclaimer,
      "${verificationIntroModel.verificationInfoModel.symbol}$amount"
    )
  }

  override fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    this.isStored = paymentInfoModel.isStored

    prepareCardComponent(paymentInfoModel, forget)
    handleLayoutVisibility(isStored)
    setStoredPaymentInformation(isStored)
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    views.changeCardButton.visibility = if (isStored) View.VISIBLE else View.GONE
    if (isStored) {
      view?.let { KeyboardUtils.showKeyboard(it) }
    }
  }

  private fun prepareCardComponent(
    paymentInfoModel: PaymentInfoModel,
    forget: Boolean
  ) {
    if (forget) adyenCardView.clear(this)
    val cardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    views.adyenCardForm.adyenCardFormPreSelected.attach(cardComponent, this)
    cardComponent.observe(this) {
      adyenCardView.setError(null)
      if (it != null && it.isValid) {
        views.submit.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          paymentDataSubject.onNext(
            AdyenCardWrapper(
              paymentMethod,
              adyenCardView.cardSave,
              hasCvc,
              paymentInfoModel.supportedShopperInteractions
            )
          )
        }
      } else {
        views.submit.isEnabled = false
      }
    }
  }

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      views.adyenCardForm.adyenCardFormPreSelectedNumber.text = adyenCardView.cardNumber
      views.adyenCardForm.adyenCardFormPreSelectedNumber.visibility = View.VISIBLE
      views.adyenCardForm.paymentMethodIc.setImageDrawable(adyenCardView.cardImage)
    } else {
      views.adyenCardForm.adyenCardFormPreSelectedNumber.visibility = View.GONE
      views.adyenCardForm.paymentMethodIc.visibility = View.GONE
    }
  }

  override fun getCancelClicks() = RxView.clicks(views.cancel)

  override fun getSubmitClicks() = RxView.clicks(views.submit)

  override fun forgetCardClick(): Observable<Any> = RxView.clicks(views.changeCardButton)

  override fun getTryAgainClicks() = RxView.clicks(views.fragmentAdyenError.tryAgain)

  override fun retryClick() = RxView.clicks(views.noNetwork.retryButton)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(views.fragmentAdyenError.layoutSupportLogo), RxView.clicks(views.fragmentAdyenError.layoutSupportIcn))
  }

  override fun showLoading() {
    views.noNetwork.root.visibility = View.GONE
    views.fragmentAdyenError.root.visibility = View.GONE
    views.contentContainer.visibility = View.GONE
    views.progressBar.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    views.progressBar.visibility = View.GONE
    views.contentContainer.visibility = View.VISIBLE
    activityView.hideLoading()
  }

  override fun showError(errorType: VerificationPaymentModel.ErrorType?) {
    if (errorType == VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS) {
      showSpecificError(R.string.verification_error_attempts_reached)
    } else {
      showSpecificError(R.string.unknown_error)
    }
  }

  override fun showGenericError() = showSpecificError(R.string.unknown_error)

  override fun showNetworkError() {
    unlockRotation()
    views.progressBar.visibility = View.GONE
    views.contentContainer.visibility = View.GONE
    views.noNetwork.root.visibility = View.VISIBLE
  }

  override fun showSpecificError(stringRes: Int) {
    unlockRotation()
    views.progressBar.visibility = View.GONE
    views.contentContainer.visibility = View.GONE

    val message = getString(stringRes)
    views.fragmentAdyenError.errorMessage.text = message
    views.fragmentAdyenError.root.visibility = View.VISIBLE
  }

  override fun showCvvError() {
    unlockRotation()
    views.progressBar.visibility = View.GONE
    views.submit.isEnabled = false
    if (isStored) {
      views.changeCardButton.visibility = View.VISIBLE
    } else {
      views.changeCardButton.visibility = View.INVISIBLE
    }
    views.contentContainer.visibility = View.VISIBLE
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }

  override fun retrievePaymentData() = paymentDataSubject

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  override fun lockRotation() = activityView.lockRotation()

  override fun unlockRotation() = activityView.unlockRotation()

  override fun cancel() = activityView.cancel()

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}