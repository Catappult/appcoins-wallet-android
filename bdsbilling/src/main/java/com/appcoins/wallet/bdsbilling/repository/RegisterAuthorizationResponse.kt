package com.appcoins.wallet.bdsbilling.repository

data class RegisterAuthorizationResponse(val uid: String, val type: String, val status: String,
                                         val data: String)
