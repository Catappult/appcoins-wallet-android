package com.asfoundation.wallet.ui

import java.io.Serializable

data class PaymentNavigationData(val paymentId: String, val paymentLabel: String,
                                 val paymentIconUrl: String, val async: Boolean,
                                 val isPreselected: Boolean) : Serializable
