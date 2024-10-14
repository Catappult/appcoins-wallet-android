package com.asfoundation.wallet.promotions.usecases

import androidx.work.WorkManager
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorker
import io.reactivex.Single
import javax.inject.Inject

class StartVipReferralPollingUseCase @Inject constructor(
  private val workManager: WorkManager,
) {

  operator fun invoke(wallet: Wallet): Single<Wallet> =
    Single.create {
      GetVipReferralWorker.enqueueUniqueWork(workManager, wallet)

      return@create it.onSuccess(wallet)
    }.doOnError { it.printStackTrace() }

}
