package com.asfoundation.wallet.repository

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofWriter
import com.asfoundation.wallet.service.CampaignService
import io.reactivex.Single

open class BdsBackEndWriter(private val defaultWalletInteract: FindDefaultWalletInteract,
                            private val service: CampaignService) : ProofWriter {

  override fun writeProof(proof: Proof): Single<String> {
    return defaultWalletInteract.find()
        .flatMap { wallet -> service.submitProof(proof, wallet.address) }
  }
}
