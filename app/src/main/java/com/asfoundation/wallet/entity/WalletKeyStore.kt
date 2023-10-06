package com.asfoundation.wallet.entity

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class WalletKeyStore(val name: String?, val contents: String) : Serializable