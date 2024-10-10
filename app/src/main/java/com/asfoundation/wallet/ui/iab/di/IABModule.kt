package com.asfoundation.wallet.ui.iab.di

import com.appcoins.wallet.core.utils.jvm_common.Repository
import com.asfoundation.wallet.ui.iab.AppCoinsOperation
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface IABModule {

  @Binds
  fun bindRepository(repository: AppCoinsOperationRepository) : Repository<String, AppCoinsOperation>
}