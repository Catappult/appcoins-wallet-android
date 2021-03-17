package com.asfoundation.wallet.ewt

import cm.aptoide.skills.interfaces.EwtObtainer
import io.reactivex.Single

class DefaultEwtObtainer(private val ewtAuthenticatorService: EwtAuthenticatorService) :
    EwtObtainer {

  override fun getEWT(): Single<String> {
    return ewtAuthenticatorService.getEwtAuthentication()
  }
}