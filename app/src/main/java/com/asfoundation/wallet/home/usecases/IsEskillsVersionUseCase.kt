package com.asfoundation.wallet.home.usecases

import cm.aptoide.skills.usecase.GetIsEskillsVersionUseCase
import cm.aptoide.skills.usecase.HasCheckedEskillsVersionUseCase
import cm.aptoide.skills.usecase.SetIsEskillsVersionUseCase
import com.appcoins.wallet.core.analytics.analytics.partners.OemIdExtractorService
import com.appcoins.wallet.core.utils.properties.MiscProperties
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named


class IsEskillsVersionUseCase @Inject constructor(
  @Named("package-name") private val walletPackageName: String,
  private val extractorService: OemIdExtractorService,
  private val hasCheckedEskillsVersionUseCase: HasCheckedEskillsVersionUseCase,
  private val getIsEskillsVersionUseCase: GetIsEskillsVersionUseCase,
  private val setIsEskillsVersionUseCase: SetIsEskillsVersionUseCase,
) {

  operator fun invoke(): Single<Boolean> {
    return if (hasCheckedEskillsVersionUseCase()) {
      Single.just(getIsEskillsVersionUseCase())
    } else extractorService.extractOemId(walletPackageName)
      .map { oemId -> MiscProperties.ESKILLS_OEM_IDS.contains(oemId) }
      .doOnSuccess { isEskillsVersion ->
        setIsEskillsVersionUseCase(isEskillsVersion)
      }.subscribeOn(Schedulers.io())
  }
}