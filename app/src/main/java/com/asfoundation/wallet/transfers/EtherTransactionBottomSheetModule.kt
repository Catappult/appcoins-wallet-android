package com.asfoundation.wallet.transfers

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.main.MainActivityNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@InstallIn(FragmentComponent::class)
@Module
class EtherTransactionBottomSheetModule {

  @Provides
  fun providesEtherTransactionBottomSheetPresenter(fragment: Fragment,
                                                   navigator: EtherTransactionBottomSheetNavigator,
                                                   data: EtherTransactionBottomSheetData): EtherTransactionBottomSheetPresenter {
    return EtherTransactionBottomSheetPresenter(fragment as EtherTransactionBottomSheetView,
        navigator, CompositeDisposable(), AndroidSchedulers.mainThread(), data)
  }

  @Provides
  fun providesEtherTransactionsBottomSheetData(
      fragment: Fragment): EtherTransactionBottomSheetData {
    fragment.requireArguments()
        .apply {
          return EtherTransactionBottomSheetData(
              getString(EtherTransactionBottomSheetFragment.TRANSACTION_KEY) as String)
        }
  }
}