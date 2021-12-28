package com.asfoundation.wallet.recover

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase
import dagger.Module
import dagger.Provides

@Module
class RecoverWalletModule {

  @Provides
  fun providesRecoverWalletViewModelFactory(getFilePathUseCase: GetFilePathUseCase,
                                            readFileUseCase: ReadFileUseCase,
                                            setDefaultWalletUseCase: SetDefaultWalletUseCase,
                                            isKeystoreUseCase: IsKeystoreUseCase,
                                            recoverKeystoreUseCase: RecoverKeystoreUseCase,
                                            recoverPrivateKeyUseCase: RecoverPrivateKeyUseCase,
                                            updateWalletInfoUseCase: UpdateWalletInfoUseCase,
                                            walletsEventSender: WalletsEventSender,
                                            rxSchedulers: RxSchedulers): RecoverWalletViewModelFactory {
    return RecoverWalletViewModelFactory(getFilePathUseCase,
        readFileUseCase, setDefaultWalletUseCase, isKeystoreUseCase, recoverKeystoreUseCase,
        recoverPrivateKeyUseCase, updateWalletInfoUseCase,
        walletsEventSender, rxSchedulers)
  }

  @Provides
  fun providesRecoverWalletNavigator(
      recoverWalletFragment: RecoverWalletFragment): RecoverWalletNavigator {
    return RecoverWalletNavigator(recoverWalletFragment)
  }
}