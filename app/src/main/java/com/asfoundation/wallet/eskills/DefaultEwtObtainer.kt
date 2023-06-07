package com.asfoundation.wallet.eskills

import cm.aptoide.skills.interfaces.EwtObtainer
import com.appcoins.wallet.feature.walletInfo.data.authentication.EwtAuthenticatorService
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
@BoundTo(supertype = EwtObtainer::class)
class DefaultEwtObtainer @Inject constructor(private val ewtAuthenticatorService: EwtAuthenticatorService) :
    EwtObtainer {

  override fun getEWT(): Single<String> {
    return ewtAuthenticatorService.getEwtAuthentication()
  }
}