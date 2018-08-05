package com.appcoins.wallet.billing.repository

data class RegisterAuthorizationResponse(val uid: String, val type: String, val status: String,
                                         val data: String)
