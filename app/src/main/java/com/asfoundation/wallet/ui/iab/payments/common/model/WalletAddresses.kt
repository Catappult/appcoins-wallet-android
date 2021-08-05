package com.asfoundation.wallet.ui.iab.payments.common.model

data class WalletAddresses(val userAddress: String, val signedAddress: String,
                           val entityOemId: String?,
                           val entityDomain: String?)