package com.asfoundation.wallet.ui.transact

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
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
  fun providesTransferFragmentData(transferFragment: TransferFragment): TransferFragmentData {
    return TransferFragmentData(transferFragment.requireContext().packageName)
  }

  @Provides
  fun providesTransferInteractor(rewardsManager: RewardsManager,
                                 getWalletInfoUseCase: GetWalletInfoUseCase,
                                 findWallet: FindDefaultWalletInteract,
                                 walletBlockedInteract: WalletBlockedInteract) =
      TransferInteractor(rewardsManager, TransactionDataValidator(), getWalletInfoUseCase,
          findWallet,
          walletBlockedInteract)

  @Provides
  fun providesTransferFragmentNavigator(fragment: Fragment,
                                        defaultTokenProvider: DefaultTokenProvider): TransferFragmentNavigator {
    return TransferFragmentNavigator(fragment.requireFragmentManager(), fragment,
        fragment.requireActivity(), defaultTokenProvider)
  }
}
