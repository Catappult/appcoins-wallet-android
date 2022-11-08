package com.asfoundation.wallet.billing.paypal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.viewModels
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentPaypalBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentPresenter
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.*
import kotlinx.android.synthetic.main.fragment_paypal.*
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PayPalIABFragment(
//  val navigatorIAB: Navigator
)
  : BasePageViewFragment(),
  SingleStateFragment<PayPalIABState, PayPalIABSideEffect> {

  @Inject
  lateinit var navigator: PayPalIABNavigator

  @Inject
  lateinit var createPaypalTransactionUseCase: CreatePaypalTransactionUseCase  //TODO

  @Inject
  lateinit var createPaypalTokenUseCase: CreatePaypalTokenUseCase  //TODO

  @Inject
  lateinit var createPaypalAgreementUseCase: CreatePaypalAgreementUseCase  //TODO

  private val viewModel: PayPalIABViewModel by viewModels()

  private var binding: FragmentPaypalBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable

  private lateinit var resultAuthLauncher: ActivityResultLauncher<Intent>

  private lateinit var iabView: IabView
  var navigatorIAB: Navigator? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentPaypalBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    registerWebViewResult()
    navigatorIAB = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Paypal payment fragment must be attached to IAB activity" }
    iabView = context
  }

  private var authenticatedToken: String? = null
  private fun registerWebViewResult() {
    resultAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.data?.dataString?.contains(PaypalReturnSchemas.RETURN.schema) == true) {   // TODO filter success and cancel results
        Log.d(this.tag, "startWebViewAuthorization SUCCESS: ${result.data ?: ""}")
        // TODO success token authentication. Store token
      } else if (
        result.resultCode == Activity.RESULT_CANCELED ||
        (result.data?.dataString?.contains(PaypalReturnSchemas.CANCEL.schema) == true)
          ) {
        Log.d(this.tag, "startWebViewAuthorization CANCELED: ${result.data ?: ""}")
        // TODO canceled login flow for token authentication

      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    handleBonusAnimation()
  }


  override fun onStateChanged(state: PayPalIABState) {
    when (state.convertTotalAsync) {
      is Async.Uninitialized -> {

      }
      is Async.Loading -> {

      }
      is Async.Fail -> {

      }
      is Async.Success -> {
        state.convertTotalAsync.value?.let { convertedTotal ->

        }
      }
    }
  }

  private fun setListeners() {
    views.attemptTransaction.setOnClickListener {
      compositeDisposable.add(
        createPaypalTransactionUseCase(
          value = (amount.toString()),
          currency = currency,
          reference = transactionBuilder.orderReference,
          origin = origin,
          packageName = transactionBuilder.domain,
          metadata = transactionBuilder.payload,
          sku = transactionBuilder.skuId,
          callbackUrl = transactionBuilder.callbackUrl,
          transactionType = transactionBuilder.type,
          developerWallet = transactionBuilder.toAddress(),
          referrerUrl = transactionBuilder.referrerUrl
        )
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
//          .filter { !waitingResult }     //TODO
          .doOnSuccess {
            Log.d(this.tag, "Successful Paypal payment ")  //TODO
            handleSuccess()
          }
          .subscribe({}, {
            Log.d(this.tag, it.toString())   //TODO
            //showGenericError()
          })
      )

    }
    views.createToken.setOnClickListener {
      compositeDisposable.add(
        createPaypalTokenUseCase()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSuccess {
            Log.d(this.tag, "Successful Token creation ")  //TODO
            authenticatedToken = it.token
            startWebViewAuthorization(it.redirect.url)
          }
          .subscribe({}, {
            Log.d(this.tag, it.toString())    //TODO
            //showGenericError()
          })
      )
    }
    views.createAgreement.setOnClickListener {
      authenticatedToken?.let { authenticatedToken ->
        compositeDisposable.add(
          createPaypalAgreementUseCase(authenticatedToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
              Log.d(this.tag, "Successful Agreement creation: ${it.uid}")
              //TODO
            }
            .subscribe({}, {
              Log.d(this.tag, it.toString())    //TODO
              //showGenericError()
            })
        )
      }
    }

  }

  private fun successBundle(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    return bundle
  }

  private fun startWebViewAuthorization(url: String) {
    val intent = WebViewActivity.newIntent(requireActivity(), url)
    resultAuthLauncher.launch(intent)
  }

  private fun handleSuccess() {
    Completable.fromAction { showSuccessAnimation() }
      .andThen(Completable.timer(getAnimationDuration(), TimeUnit.MILLISECONDS))
      .andThen(Completable.fromAction { navigatorIAB?.popView(successBundle()) })
      .subscribe()
  }

  private fun showSuccessAnimation() {
    iab_activity_transaction_completed.visibility = View.VISIBLE
    attempt_transaction.visibility = View.GONE
    create_token.visibility = View.GONE
    create_agreement.visibility = View.GONE
  }

  private fun getAnimationDuration() = lottie_transaction_success.duration

  private fun handleBonusAnimation() {
    if (StringUtils.isNotBlank(bonus)) {
      lottie_transaction_success.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation()
    } else {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(lottie_transaction_success)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    lottie_transaction_success.setTextDelegate(textDelegate)
    lottie_transaction_success.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  override fun onSideEffect(sideEffect: PayPalIABSideEffect) = Unit

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY_KEY)) {
      requireArguments().getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val origin: String? by lazy {
    if (requireArguments().containsKey(ORIGIN_KEY)) {
      requireArguments().getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_DATA_KEY)) {
      requireArguments().getParcelable<TransactionBuilder>(TRANSACTION_DATA_KEY)!!
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }


  companion object {

    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val IS_SKILLS = "is_skills"
    private const val FREQUENCY = "frequency"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"
    private const val NAVIGATOR = "navigator"

    @JvmStatic
    fun newInstance(
      paymentType: PaymentType,
      origin: String?,
      transactionBuilder: TransactionBuilder,
      amount: BigDecimal,
      currency: String?,
      bonus: String?,
      isPreSelected: Boolean,
      gamificationLevel: Int,
      skuDescription: String,
      isSubscription: Boolean,
      isSkills: Boolean,
      frequency: String?,
    ): PayPalIABFragment = PayPalIABFragment().apply {
      arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(ORIGIN_KEY, origin)
        putParcelable(TRANSACTION_DATA_KEY, transactionBuilder)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putString(SKU_DESCRIPTION, skuDescription)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putBoolean(IS_SKILLS, isSkills)
        putString(FREQUENCY, frequency)
      }
    }

  }

}
