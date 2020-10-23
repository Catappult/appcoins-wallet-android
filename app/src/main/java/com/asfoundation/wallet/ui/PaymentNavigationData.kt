package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.iab.PaymentMethod
import java.io.Serializable


data class PaymentNavigationData(val paymentMethod: PaymentMethod, val isPreselected: Boolean) :
    Serializable
