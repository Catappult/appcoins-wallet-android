package com.asfoundation.wallet.home.usecases

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import io.reactivex.Single
import javax.inject.Inject


class IsEskillsVersionUseCase @Inject constructor(
  private val extractorService: OemIdExtractorService
){

  private val oemId = mutableStateOf("")

  @SuppressLint("CheckResult")
  operator fun invoke(packageName:String): Boolean {
    if (oemId.value=="") {
      oemId.value = extractorService
        .extractOemId(packageName)
        .blockingGet()
    }
    return MiscProperties.ESKILLS_OEM_IDS.contains(oemId.value)
  }
}