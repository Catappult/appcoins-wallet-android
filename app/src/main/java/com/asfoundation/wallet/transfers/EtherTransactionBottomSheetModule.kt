package com.asfoundation.wallet.transfers

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
          getSerializable(EtherTransactionBottomSheetFragment.HASH_KEY) as String)
    }
  }

  @Provides
  fun providesEtherTransactionBottomSheetNavigator(
      fragment: EtherTransactionBottomSheetFragment): EtherTransactionBottomSheetNavigator {
    return EtherTransactionBottomSheetNavigator(fragment.requireFragmentManager(), fragment)
  }
}