package com.asfoundation.wallet.verification.ui.credit_card.error

import androidx.fragment.app.Fragment
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityNavigator
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class VerificationErrorModule {

  @Provides
  fun providesVerificationErrorPresenter(fragment: Fragment,
                                         data: VerificationErrorData,
                                         navigator: VerificationErrorNavigator): VerificationErrorPresenter {
    return VerificationErrorPresenter(fragment as VerificationErrorView, data, navigator,
        CompositeDisposable())
  }

  @Provides
  fun providesVerificationErrorData(fragment: Fragment): VerificationErrorData {
    fragment.requireArguments()
        .apply {
          return VerificationErrorData(
              VerificationCodeResult.ErrorType.values()[getInt(
                  VerificationErrorFragment.ERROR_TYPE)],
              getString(VerificationErrorFragment.AMOUNT, ""),
              getString(VerificationErrorFragment.SYMBOL, "")
          )
        }
  }
}