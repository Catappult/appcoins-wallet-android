package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.PaymentNavigationData

data class PaymentAuthenticationResult(val isSuccess: Boolean,
                                       val paymentNavigationData: PaymentNavigationData?)
