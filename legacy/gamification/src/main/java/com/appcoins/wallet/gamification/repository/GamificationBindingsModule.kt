package com.appcoins.wallet.gamification.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GamificationBindingsModule {

  @Binds
  abstract fun bindPromotionsRepository(impl: BdsPromotionsRepository): PromotionsRepository

  @Binds
  abstract fun bindUserStatsLocalData(impl: UserStatsRepository): UserStatsLocalData
}