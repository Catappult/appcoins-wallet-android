package com.asfoundation.wallet.ui.transact

import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@InstallIn(FragmentComponent::class)
@Module
class TransferFragmentModule {

  @Provides
  fun providesTransferFragmentPresenter(fragment: Fragment,
                                        getWalletInfoUseCase: GetWalletInfoUseCase,
                                        interactor: TransferInteractor,
                                        data: TransferFragmentData,
                                        navigator: TransferFragmentNavigator,
                                        currencyFormatUtils: CurrencyFormatUtils): TransferFragmentPresenter {
    return TransferFragmentPresenter(fragment as TransferFragmentView,
        CompositeDisposable(), CompositeDisposable(), getWalletInfoUseCase, interactor, navigator,
        Schedulers.io(), AndroidSchedulers.mainThread(), data, currencyFormatUtils)
  }

  @Provides
  fun providesTransferFragmentData(fragment: Fragment): TransferFragmentData {
    return TransferFragmentData(fragment.requireContext().packageName)
  }
}
