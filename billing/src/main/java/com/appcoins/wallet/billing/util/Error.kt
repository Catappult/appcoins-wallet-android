package com.appcoins.wallet.billing.util

data class Error(val hasError: Boolean = false, val isNetworkError: Boolean = false,
                 val code: Int? = null, val message: String? = null)