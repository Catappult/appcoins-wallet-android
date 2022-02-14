package com.asfoundation.wallet.analytics

import android.content.Context
import android.telephony.TelephonyManager
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.identification.IdsRepository
import com.uxcam.UXCam
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class UxCamUtils (private val context: Context, private val idsRepository: IdsRepository) {

  /**
   * list of countries to use uxcam
   */
  val countriesFilterList = listOf(
    "RU",
    "US",
    "PH",
    "IN",
    "ID",
    "PT"
  )

  /** Time allowed to keep the same session when the user exits the app. For example, when checking
   * etherscan or navigating file explorer.
   */
  private val timeBreak = 20_000    //20s

  fun initialize(): Completable? {
    //val countryCode: String = context.resources.configuration.locale.getCountry()   // to use system language instead of carrier

    // countries are being filter based on the sim carrier, phones with no sim will be excluded
    val tm: TelephonyManager? = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val countryCode: String = tm?.networkCountryIso?.toUpperCase() ?: ""

    if (countriesFilterList.contains(countryCode)) {
      UXCam.startWithKey(BuildConfig.UXCAM_API_KEY)

      UXCam.occludeAllTextFields(true)
      UXCam.allowShortBreakForAnotherApp(true)
      UXCam.allowShortBreakForAnotherApp(timeBreak)

      return Single.just(idsRepository.getActiveWalletAddress())
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .doOnSuccess { walletAddress ->
          UXCam.setUserIdentity(walletAddress)
        }
        .ignoreElement()
    }
    return null
  }

  companion object {
    fun hideScreen(hide: Boolean) {
      UXCam.occludeSensitiveScreen(hide)
    }
  }
}