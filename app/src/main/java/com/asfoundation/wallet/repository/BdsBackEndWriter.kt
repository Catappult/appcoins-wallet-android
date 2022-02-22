package com.asfoundation.wallet.repository

import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofWriter
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = ProofWriter::class)
open class BdsBackEndWriter @Inject constructor(
    private val defaultWalletInteract: FindDefaultWalletInteract,
    private val service: CampaignService) : ProofWriter {

  override fun writeProof(proof: Proof): Single<String> {
    return defaultWalletInteract.find()
        .flatMap { wallet -> service.submitProof(proof, wallet.address) }
  }
}
