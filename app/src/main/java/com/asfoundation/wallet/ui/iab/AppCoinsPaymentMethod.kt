package com.asfoundation.wallet.ui.iab

data class AppCoinsPaymentMethod(override val id: String, override val label: String,
                                 override val iconUrl: String,
                                 override val isEnabled: Boolean = false,
                                 val isAppcEnabled: Boolean = false,
                                 val isCreditsEnabled: Boolean = false, val appcLabel: String,
                                 val creditsLabel: String, val creditsIconUrl: String,
                                 override var disabledReason: Int? = null,
                                 val disabledReasonAppc: Int? = null,
                                 val disabledReasonCredits: Int? = null) :
    PaymentMethod(id, label, iconUrl, false, null, isEnabled)