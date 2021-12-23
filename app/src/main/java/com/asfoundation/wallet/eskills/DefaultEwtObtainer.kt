package com.asfoundation.wallet.eskills

import cm.aptoide.skills.interfaces.EwtObtainer
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import io.reactivex.Single

class DefaultEwtObtainer(private val ewtAuthenticatorService: EwtAuthenticatorService) :
    EwtObtainer {

  override fun getEWT(): Single<String> {
    return ewtAuthenticatorService.getEwtAuthentication()
  }
}