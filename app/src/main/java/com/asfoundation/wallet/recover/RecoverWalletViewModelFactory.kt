package com.asfoundation.wallet.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.recover.use_cases.*
import com.asfoundation.wallet.wallets.usecases.UpdateWalletInfoUseCase

class RecoverWalletViewModelFactory(private val getFilePathUseCase: GetFilePathUseCase,
                                    private val readFileUseCase: ReadFileUseCase,
                                    private val setDefaultWalletUseCase: SetDefaultWalletUseCase,
                                    private val isKeystoreUseCase: IsKeystoreUseCase,
                                    private val recoverKeystoreUseCase: RecoverKeystoreUseCase,
                                    private val recoverPrivateKeyUseCase: RecoverPrivateKeyUseCase,
                                    private val updateWalletInfoUseCase: UpdateWalletInfoUseCase,
                                    private val walletsEventSender: WalletsEventSender,
                                    private val rxSchedulers: RxSchedulers) :
    ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return RecoverWalletViewModel(getFilePathUseCase, readFileUseCase,
        setDefaultWalletUseCase, isKeystoreUseCase, recoverKeystoreUseCase,
        recoverPrivateKeyUseCase, updateWalletInfoUseCase, walletsEventSender,
        rxSchedulers) as T
  }
}