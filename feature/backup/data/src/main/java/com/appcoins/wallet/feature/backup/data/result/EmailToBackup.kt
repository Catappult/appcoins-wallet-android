package com.appcoins.wallet.feature.backup.data.result

import java.io.Serializable

data class EmailToBackup (
  val validity: ValidityEmailState?,
) : Serializable

enum class ValidityEmailState(val value: Int) {
  INVALID(0),
  UNKNOWN(1),
  VALID(2);


  companion object {
    fun toEnum(value: Int) = ValidityEmailState.values().firstOrNull { it.value == value }
  }
}