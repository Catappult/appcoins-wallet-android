package com.asfoundation.wallet.iab.di

import androidx.navigation.NavController
import com.asfoundation.wallet.iab.PaymentActivityNavigator
import dagger.assisted.AssistedFactory

@AssistedFactory
interface PaymentActivityNavigatorFactory {

  fun create(navController: NavController): PaymentActivityNavigator
}
