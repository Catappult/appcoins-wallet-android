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
    Log.d("Package name", packageName)
    if (oemId.value=="") {
      Log.d("Extract OEMID", "Entra no Extract")
      oemId.value = extractorService
        .extractOemId(packageName)
        .blockingGet()
      Log.d("Extract OEMID", oemId.value)
    }
    oemId.value = "01b4b7f957b2d8ff96a0f742fd5e41fa"
    Log.d("Extract OEMID", oemId.value)
    return MiscProperties.ESKILLS_OEM_IDS.contains(oemId.value)
  }
}