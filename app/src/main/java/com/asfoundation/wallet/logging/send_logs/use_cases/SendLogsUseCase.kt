package com.asfoundation.wallet.logging.send_logs.use_cases

import android.annotation.SuppressLint
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import io.reactivex.schedulers.Schedulers

class SendLogsUseCase(
    private val sendLogsRepository: SendLogsRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

    @SuppressLint("CheckResult")
    operator fun invoke() {
        getCurrentWalletUseCase()
            .subscribeOn(Schedulers.io())
            .map{ sendLogsRepository.sendLogs(it.address) }
    }
}