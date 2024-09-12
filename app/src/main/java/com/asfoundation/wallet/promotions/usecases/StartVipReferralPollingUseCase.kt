package com.asfoundation.wallet.promotions.usecases

import androidx.work.ExistingWorkPolicy
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
      workManager.enqueueUniqueWork(
        GetVipReferralWorker.getUniqueName(wallet),
        ExistingWorkPolicy.REPLACE,
        GetVipReferralWorker.getWorkRequest()
      )
      return@create it.onSuccess(wallet)
    }.doOnError { it.printStackTrace() }

}
