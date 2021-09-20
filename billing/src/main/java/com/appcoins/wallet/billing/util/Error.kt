package com.appcoins.wallet.billing.util

import com.appcoins.wallet.billing.ErrorInfo

data class Error(val hasError: Boolean = false, val isNetworkError: Boolean = false,
                 val errorInfo: ErrorInfo? = null)

