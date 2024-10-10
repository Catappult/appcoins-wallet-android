package com.asfoundation.wallet.ui.iab.localpayments

import androidx.fragment.app.Fragment
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.core.utils.jvm_common.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal

@InstallIn(FragmentComponent::class)
@Module
class LocalPaymentModule {

  @Provides
  fun providesLocalPaymentPresenter(
    fragment: Fragment,
    data: LocalPaymentData,
    interactor: LocalPaymentInteractor,
    navigator: LocalPaymentNavigator,
    analytics: LocalPaymentAnalytics,
    logger: Logger,
    errorMapper: ErrorMapper
  ): LocalPaymentPresenter {
    return LocalPaymentPresenter(
      fragment as LocalPaymentView, data, interactor, navigator,
      analytics, AndroidSchedulers.mainThread(), Schedulers.io(), CompositeDisposable(),
      fragment.context, logger, errorMapper
    )
  }

  @Provides
  fun providesLocalPaymentData(fragment: Fragment) =
    fragment.requireArguments().run {
      LocalPaymentData(
        packageName = getString(LocalPaymentFragment.DOMAIN_KEY)!!,
        skuId = getString(LocalPaymentFragment.SKU_ID_KEY),
        fiatAmount = getString(LocalPaymentFragment.ORIGINAL_AMOUNT_KEY),
        currency = getString(LocalPaymentFragment.CURRENCY_KEY),
        bonus = getString(LocalPaymentFragment.BONUS_KEY),
        paymentId = getString(LocalPaymentFragment.PAYMENT_KEY)!!,
        developerAddress = getString(LocalPaymentFragment.DEV_ADDRESS_KEY)!!,
        type = getString(LocalPaymentFragment.TYPE_KEY)!!,
        appcAmount = getSerializableExtra<BigDecimal>(LocalPaymentFragment.AMOUNT_KEY)!!,
        callbackUrl = getString(LocalPaymentFragment.CALLBACK_URL),
        orderReference = getString(LocalPaymentFragment.ORDER_REFERENCE),
        payload = getString(LocalPaymentFragment.PAYLOAD),
        origin = getString(LocalPaymentFragment.ORIGIN),
        paymentMethodIconUrl = getString(LocalPaymentFragment.PAYMENT_METHOD_URL),
        label = getString(LocalPaymentFragment.PAYMENT_METHOD_LABEL),
        async = getBoolean(LocalPaymentFragment.ASYNC),
        referrerUrl = getString(LocalPaymentFragment.REFERRER_URL),
        gamificationLevel = getInt(LocalPaymentFragment.GAMIFICATION_LEVEL),
        guestWalletId = getString(LocalPaymentFragment.GUEST_WALLET_ID),
      )
    }
}