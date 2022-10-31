package com.asfoundation.wallet.billing.paypal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.asf.wallet.databinding.FragmentPaypalBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.billing.adyen.AdyenPaymentPresenter
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class PayPalIABFragment : BasePageViewFragment(),
  SingleStateFragment<PayPalIABState, PayPalIABSideEffect> {

  @Inject
  lateinit var navigator: PayPalIABNavigator

  @Inject
  lateinit var createPaypalTransactionUseCase: CreatePaypalTransactionUseCase  //TODO

  private val viewModel: PayPalIABViewModel by viewModels()

  private var binding: FragmentPaypalBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentPaypalBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    return views.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()

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
    //views.topBar?.barBackButton?.setOnClickListener { navigator.navigateBack() }
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
            Log.d(this.tag, "Successful Paypal payment ")
          }
          .subscribe({}, {
            Log.d(this.tag, it.toString())
            //showGenericError()
          })
      )

    }
    views.createToken.setOnClickListener {

    }
    views.createAgreement.setOnClickListener {

    }

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
      frequency: String?
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
