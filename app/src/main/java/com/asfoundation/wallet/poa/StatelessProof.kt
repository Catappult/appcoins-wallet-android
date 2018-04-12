package com.asfoundation.wallet.poa

data class StatelessProof(val packageName: String, val campaignId: String,
                          val proofComponentList: List<ProofComponent>,
                          val proofId: String?, val walletPackage: String)