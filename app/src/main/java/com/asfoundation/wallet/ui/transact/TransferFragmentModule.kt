package com.asfoundation.wallet.ui.transact

import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import com.asfoundation.wallet.wallets.GetDefaultWalletBalanceInteract
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@Module
class TransferFragmentModule {

  @Provides
  fun providesTransferFragmentPresenter(transferFragment: TransferFragment,
                                        interactor: TransferInteractor,
                                        data: TransferFragmentData,
                                        navigator: TransferFragmentNavigator,
                                        currencyFormatUtils: CurrencyFormatUtils): TransferFragmentPresenter {
    return TransferFragmentPresenter(transferFragment as TransferFragmentView,
        CompositeDisposable(), CompositeDisposable(), interactor, navigator, Schedulers.io(),
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
  fun providesTransferFragmentNavigator(transferFragment: TransferFragment,
                                        defaultTokenProvider: DefaultTokenProvider): TransferFragmentNavigator {
    return TransferFragmentNavigator(transferFragment.requireFragmentManager(), transferFragment,
        transferFragment.activity!!, defaultTokenProvider)
  }
}
