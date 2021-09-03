package com.asfoundation.wallet.verification.error

import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asfoundation.wallet.verification.VerificationActivityNavigator
import com.asfoundation.wallet.verification.VerificationActivityView
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable

@Module
class VerificationErrorModule {

  @Provides
  fun providesVerificationErrorPresenter(fragment: VerificationErrorFragment,
                                         data: VerificationErrorData,
                                         navigator: VerificationErrorNavigator): VerificationErrorPresenter {
    return VerificationErrorPresenter(fragment as VerificationErrorView, data, navigator,
        CompositeDisposable())
  }

  @Provides
  fun providesVerificationErrorNavigator(fragment: VerificationErrorFragment,
                                         activityNavigator: VerificationActivityNavigator): VerificationErrorNavigator {
    return VerificationErrorNavigator(fragment.requireFragmentManager(),
        fragment.activity as VerificationActivityView, activityNavigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      fragment: VerificationErrorFragment): VerificationActivityNavigator {
    return VerificationActivityNavigator(fragment.requireActivity(),
        fragment.requireActivity().supportFragmentManager)
  }

  @Provides
  fun providesVerificationErrorData(fragment: VerificationErrorFragment): VerificationErrorData {
    fragment.arguments!!.apply {
      return VerificationErrorData(
          VerificationCodeResult.ErrorType.values()[getInt(VerificationErrorFragment.ERROR_TYPE)],
          getString(VerificationErrorFragment.AMOUNT, ""),
          getString(VerificationErrorFragment.SYMBOL, "")
      )
    }
  }
}