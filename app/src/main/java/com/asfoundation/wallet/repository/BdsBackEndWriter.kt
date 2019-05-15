package com.asfoundation.wallet.repository

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofSubmissionFeeData
import com.asfoundation.wallet.poa.ProofWriter
import com.asfoundation.wallet.service.PoASubmissionService
import io.reactivex.Single
import java.math.BigDecimal
import java.net.UnknownHostException

open class BdsBackEndWriter(
    private val defaultWalletInteract: FindDefaultWalletInteract,
    private val service: PoASubmissionService) : ProofWriter {

  override fun writeProof(proof: Proof): Single<String> {
    return defaultWalletInteract.find()
        .flatMap { wallet -> service.submitProof(proof, wallet.address) }
  }

  override fun hasWalletPrepared(chainId: Int): Single<ProofSubmissionFeeData> {
    if (!isCorrectNetwork(chainId)) {
      if (isKnownNetwork(chainId)) {
        return Single.just(
            ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.WRONG_NETWORK,
                BigDecimal.ZERO, BigDecimal.ZERO));
      } else {
        return Single.just(
            ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.UNKNOWN_NETWORK,
                BigDecimal.ZERO, BigDecimal.ZERO));
      }

    }
    return defaultWalletInteract.find().map {
      ProofSubmissionFeeData(ProofSubmissionFeeData.RequirementsStatus.READY,
          BigDecimal.ZERO, BigDecimal.ZERO)
    }.onErrorReturn {
      when (it) {
        is WalletNotFoundException -> ProofSubmissionFeeData(
            ProofSubmissionFeeData.RequirementsStatus.NO_WALLET,
            BigDecimal.ZERO, BigDecimal.ZERO)
        is UnknownHostException -> ProofSubmissionFeeData(
            ProofSubmissionFeeData.RequirementsStatus.NO_NETWORK,
            BigDecimal.ZERO, BigDecimal.ZERO)
        else -> throw it
      }
    }
  }

  private fun isKnownNetwork(chainId: Int): Boolean {
    return chainId == 1 || chainId == 3;
  }

  private fun isCorrectNetwork(chainId: Int): Boolean {
    return chainId == 3 && BuildConfig.DEBUG || chainId == 1 && !BuildConfig.DEBUG
  }
}
