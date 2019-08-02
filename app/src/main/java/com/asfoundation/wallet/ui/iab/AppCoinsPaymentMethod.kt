package com.asfoundation.wallet.ui.iab

data class AppCoinsPaymentMethod(override val id: String, override val label: String,
                                 override val iconUrl: String,
                                 override val isEnabled: Boolean = false,
                                 val isAppcEnabled: Boolean = false,
                                 val isCreditsEnabled: Boolean = false) :
    PaymentMethod(id, label, iconUrl, isEnabled)