package com.asfoundation.wallet.onboarding_new_payment.payment_result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingSharedHeaderViewModel:  ViewModel() {
    val viewVisibility = MutableLiveData<Int>()
}