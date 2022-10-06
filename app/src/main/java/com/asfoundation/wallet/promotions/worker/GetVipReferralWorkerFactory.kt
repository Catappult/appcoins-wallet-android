package com.asfoundation.wallet.promotions.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

class GetVipReferralWorkerFactory @Inject constructor(
  private val getVipReferralWorker: GetVipReferralWorker.Factory,
) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    return when (workerClassName) {
      GetVipReferralWorker::class.java.name ->
        getVipReferralWorker.create(appContext, workerParameters)
      else -> null
    }
  }
}