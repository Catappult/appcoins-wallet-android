package com.appcoins.wallet.core.network.microservices.model

/**
 * Application Info
 * @param name Package name
 * @param title name of the application (not translated)
 * @param icon icon url
 */
data class ApplicationInfoResponse(val name: String, val title: String, val icon: String)
