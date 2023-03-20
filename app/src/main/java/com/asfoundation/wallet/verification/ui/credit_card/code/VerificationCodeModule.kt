package com.asfoundation.wallet.verification.ui.credit_card.code

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.verification.ui.credit_card.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class VerificationCodeModule {

  @Provides
  fun providesWalletVerificationCodePresenter(fragment: Fragment,
                                              data: VerificationCodeData,
                                              activityData: VerificationCreditCardActivityData,
                                              verificationCodeInteractor: VerificationCodeInteractor,
                                              verificationCodeNavigator: VerificationCodeNavigator,
                                              logger: Logger,
                                              analytics: VerificationAnalytics): VerificationCodePresenter {
    return VerificationCodePresenter(fragment as VerificationCodeView, data, activityData,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io(),
        verificationCodeInteractor, verificationCodeNavigator, logger, analytics)
  }

  @Provides
  fun providesVerificationCodeData(fragment: Fragment): VerificationCodeData {
    fragment.requireArguments()
        .apply {
          return VerificationCodeData(getBoolean(VerificationCodeFragment.LOADED_KEY),
              getLong(VerificationCodeFragment.DATE_KEY),
              getString(VerificationCodeFragment.FORMAT_KEY),
              getString(VerificationCodeFragment.AMOUNT_KEY),
              getString(VerificationCodeFragment.CURRENCY_KEY),
              getString(VerificationCodeFragment.SYMBOL_KEY),
              getString(VerificationCodeFragment.PERIOD_KEY),
              getInt(VerificationCodeFragment.DIGITS_KEY)
          )
        }
  }
}