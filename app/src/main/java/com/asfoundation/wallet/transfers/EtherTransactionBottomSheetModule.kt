package com.asfoundation.wallet.transfers

import com.asfoundation.wallet.entity.NetworkInfo
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

@Module
class EtherTransactionBottomSheetModule {

  @Provides
  fun providesEtherTransactionBottomSheetPresenter(fragment: EtherTransactionBottomSheetFragment,
                                                   navigator: EtherTransactionBottomSheetNavigator,
                                                   data: EtherTransactionBottomSheetData): EtherTransactionBottomSheetPresenter {
    return EtherTransactionBottomSheetPresenter(fragment as EtherTransactionBottomSheetView,
        navigator, CompositeDisposable(), AndroidSchedulers.mainThread(), data)
  }

  @Provides
  fun providesEtherTransactionsBottomSheetData(
      fragment: EtherTransactionBottomSheetFragment): EtherTransactionBottomSheetData {
    fragment.arguments!!.apply {
      return EtherTransactionBottomSheetData(
          getString(EtherTransactionBottomSheetFragment.TRANSACTION_KEY) as String)
    }
  }

  @Provides
  fun providesEtherTransactionBottomSheetNavigator(fragment: EtherTransactionBottomSheetFragment,
                                                   networkInfo: NetworkInfo): EtherTransactionBottomSheetNavigator {
    return EtherTransactionBottomSheetNavigator(fragment.requireFragmentManager(), fragment,
        networkInfo)
  }
}