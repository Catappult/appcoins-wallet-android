package com.asfoundation.wallet.poa

data class PoaInformationModel(val remainingPoa: Int, val remainingHours: Int,
                               val remainingMinutes: Int) {

  fun hasRemainingPoa() = remainingPoa != 0 && (remainingHours >= 0 || remainingMinutes >= 0)
}