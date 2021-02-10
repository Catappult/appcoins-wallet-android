package com.asfoundation.wallet.promotions.voucher

import java.util.*

class EVoucherDetailsInteractor {

  fun getDiamondModels(): List<DiamondsButtonModel> {
    val diamondsButtonModels: MutableList<DiamondsButtonModel> = LinkedList()
    diamondsButtonModels.add(DiamondsButtonModel(43, "Diamonds"))
    diamondsButtonModels.add(DiamondsButtonModel(218, "Diamonds"))
    diamondsButtonModels.add(DiamondsButtonModel(430, "Diamonds"))
    diamondsButtonModels.add(DiamondsButtonModel(43, "Diamonds"))
    diamondsButtonModels.add(DiamondsButtonModel(218, "Diamonds"))
    diamondsButtonModels.add(DiamondsButtonModel(430, "Diamonds"))
    return diamondsButtonModels
  }

  fun getTitle(): String {
    return "Voucher for Garena Free Fire: BOOYAH Day"
  }
}