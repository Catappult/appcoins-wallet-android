package com.appcoins.wallet.ui.widgets

data class GameDetailsData(
    val title: String,
    val gameIcon: String,
    val gameBackground: String?,
    val gamePackage: String,
    val description: String,
    val screenshots: List<String>?
)