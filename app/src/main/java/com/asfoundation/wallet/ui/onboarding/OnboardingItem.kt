package com.asfoundation.wallet.ui.onboarding

import androidx.annotation.StringRes

data class OnboardingItem(
    @StringRes val title: Int,
    val message: String
)