package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.GetDefaultWalletBalanceInteract
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class TransferFragmentModule {

  @Provides
  fun providesTransferPresenter(transferFragment: TransferFragment,
                                interactor: TransferInteractor,
                                data: TransferFragmentData,
                                navigator: TransferNavigator,
                                currencyFormatUtils: CurrencyFormatUtils): TransferPresenter {
    return TransferPresenter(transferFragment as TransferFragmentView, CompositeDisposable(),
        CompositeDisposable(), interactor, navigator, Schedulers.io(),
        AndroidSchedulers.mainThread(), data, currencyFormatUtils)
  }

  @Provides
  fun providesTransferFragmentData(transferFragment: TransferFragment): TransferFragmentData {
    return TransferFragmentData(transferFragment.context!!.packageName)
  }

  @Provides
  fun providesTransferInteractor(rewardsManager: RewardsManager,
                                 balance: GetDefaultWalletBalanceInteract,
                                 findWallet: FindDefaultWalletInteract,
                                 walletBlockedInteract: WalletBlockedInteract) =
      TransferInteractor(rewardsManager, TransactionDataValidator(), balance, findWallet,
          walletBlockedInteract)

  @Provides
  fun providesTransferNavigator(transferFragment: TransferFragment,
                                defaultTokenProvider: DefaultTokenProvider): TransferNavigator {
    return TransferNavigator(transferFragment.requireFragmentManager(), transferFragment,
        transferFragment.activity!!, defaultTokenProvider)
  }
}
