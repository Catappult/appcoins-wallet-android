package com.asfoundation.wallet.promotions.usecases

import androidx.work.WorkManager
import com.appcoins.wallet.core.network.backend.model.VipReferralResponse
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorker
import io.reactivex.Single
import javax.inject.Inject

class CheckAndCancelVipPollingUseCase @Inject constructor(
  private val getVipReferralUseCase: GetVipReferralUseCase,
  private val workManager: WorkManager
) {

  operator fun invoke(wallet: Wallet): Single<VipReferralResponse> {
    return getVipReferralUseCase(wallet)
      .doOnSuccess {
        if (it.active) {
          workManager.cancelUniqueWork(GetVipReferralWorker.getUniqueName(wallet))
        }
      }
  }

}
