package com.asfoundation.wallet.service

import androidx.annotation.StringRes

interface ServicesErrorMapper {
  @StringRes
  fun mapError(errorCode: Int): Int
}
