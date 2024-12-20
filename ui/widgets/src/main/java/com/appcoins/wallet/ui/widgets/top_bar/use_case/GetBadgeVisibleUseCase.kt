package com.appcoins.wallet.ui.widgets.top_bar.use_case

import kotlinx.coroutines.flow.Flow

interface GetBadgeVisibleUseCase {

  operator fun invoke(): Flow<Boolean>
}