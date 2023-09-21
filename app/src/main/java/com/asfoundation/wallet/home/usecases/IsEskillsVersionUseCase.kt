package com.asfoundation.wallet.home.usecases

import cm.aptoide.skills.usecase.GetIsEskillsVersionUseCase
import cm.aptoide.skills.usecase.HasCheckedEskillsVersionUseCase
import cm.aptoide.skills.usecase.SetIsEskillsVersionUseCase
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class IsEskillsVersionUseCase @Inject constructor(
  private val extractorService: OemIdExtractorService,
  private val hasCheckedEskillsVersionUseCase: HasCheckedEskillsVersionUseCase,
  private val getIsEskillsVersionUseCase: GetIsEskillsVersionUseCase,
  private val setIsEskillsVersionUseCase: SetIsEskillsVersionUseCase,
) {

  operator fun invoke(packageName: String): Single<Boolean> {
    return if (hasCheckedEskillsVersionUseCase()) {
      Single.just(getIsEskillsVersionUseCase())
    } else extractorService.extractOemId(packageName)
      .map { oemId -> MiscProperties.ESKILLS_OEM_IDS.contains(oemId) }
      .doOnSuccess { isEskillsVersion ->
        setIsEskillsVersionUseCase(isEskillsVersion)
      }.subscribeOn(Schedulers.io())
  }
}