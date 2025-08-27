package com.asfoundation.wallet.ui.webview_payment.models

import com.google.gson.annotations.SerializedName

data class CloseBehaviorConfig(
    @SerializedName("lockCloseView") val lockCloseView: Boolean = false,
    @SerializedName("lockBackButton") val lockBackButton: Boolean = false,
    @SerializedName("timeout") val timeout: Int = 0
)
