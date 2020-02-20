package com.asfoundation.wallet.support

import androidx.work.RxWorker
import androidx.work.WorkerParameters
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Provider

@Subcomponent(modules = [SupportNotificationWorker.Builder::class])
interface WorkerSubComponent {
  fun workers(): Map<Class<out RxWorker>, Provider<RxWorker>>
  @Subcomponent.Builder
  interface Builder {
    @BindsInstance
    fun workerParameters(param: WorkerParameters): Builder

    fun build(): WorkerSubComponent
  }
}