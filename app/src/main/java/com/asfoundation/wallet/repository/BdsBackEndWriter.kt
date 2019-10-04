package com.asfoundation.wallet.repository

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofSubmissionData
import com.asfoundation.wallet.poa.ProofWriter
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.CampaignStatus
import io.reactivex.Single
import java.net.UnknownHostException

open class BdsBackEndWriter(
    private val defaultWalletInteract: FindDefaultWalletInteract,
    private val service: CampaignService) : ProofWriter {

  override fun writeProof(proof: Proof): Single<String> {
    return defaultWalletInteract.find()
        .flatMap { wallet -> service.submitProof(proof, wallet.address) }
  }

  override fun hasWalletPrepared(chainId: Int,
                                 packageName: String,
                                 versionCode: Int): Single<ProofSubmissionData>? {
    if (!isCorrectNetwork(chainId)) {
      return if (isKnownNetwork(chainId)) {
        Single.just(
            ProofSubmissionData(ProofSubmissionData.RequirementsStatus.WRONG_NETWORK))
      } else {
        Single.just(
            ProofSubmissionData(ProofSubmissionData.RequirementsStatus.UNKNOWN_NETWORK))
      }
    }

    return defaultWalletInteract.find()
        .flatMap {
          service.getCampaign(it.address,
              packageName, versionCode)
        }
        .map {
          if (isEligible(it.campaignStatus)) {
            ProofSubmissionData(ProofSubmissionData.RequirementsStatus.READY)
          } else {
            ProofSubmissionData(ProofSubmissionData.RequirementsStatus.NOT_ELIGIBLE,
                it.hoursRemaining, it.minutesRemaining)
          }
        }
        .onErrorReturn {
          when (it) {
            is WalletNotFoundException -> ProofSubmissionData(
                ProofSubmissionData.RequirementsStatus.NO_WALLET)
            is UnknownHostException -> ProofSubmissionData(
                ProofSubmissionData.RequirementsStatus.NO_NETWORK)
            else -> throw it
          }
        }
  }

  private fun isEligible(campaignStatus: CampaignStatus): Boolean {
    return campaignStatus == CampaignStatus.AVAILABLE
  }

  private fun isKnownNetwork(chainId: Int): Boolean {
    return chainId == 1 || chainId == 3
  }

  private fun isCorrectNetwork(chainId: Int): Boolean {
    return chainId == 3 && BuildConfig.DEBUG || chainId == 1 && !BuildConfig.DEBUG
  }
}
