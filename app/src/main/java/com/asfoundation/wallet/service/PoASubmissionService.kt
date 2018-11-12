package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.SubmitPoAException
import com.asfoundation.wallet.entity.SubmitPoAResponse
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofComponent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.http.*

class PoASubmissionService(private val poaSubmissionApi: PoASubmissionService.PoASubmissionApi) {

  companion object {
    const val SERVICE_HOST = BuildConfig.BACKEND_HOST
  }

  fun submitProof(proof: Proof, wallet: String): Single<String> {
    return poaSubmissionApi.submitProof(
        SerializedProof(proof.campaignId, proof.packageName, wallet, proof.proofComponentList,
            proof.storeAddress, proof.oemAddress))
        .map { response -> handleResponse(response) }
        .subscribeOn(Schedulers.io())
        .singleOrError()
  }

  private fun handleResponse(response: SubmitPoAResponse): String {
    if (response.isValid) {
      return response.transactionId
    } else {
      throw SubmitPoAException(response.errorCode)
    }
  }

  interface PoASubmissionApi {
    @Headers("Content-Type: application/json")
    @POST("/campaign/submitpoa")
    fun submitProof(@Body body: SerializedProof?): Observable<SubmitPoAResponse>
  }
}

class SerializedProof(val bid_id: String?,
                      val package_name: String,
                      val address: String,
                      val nonces: List<ProofComponent>,
                      val store: String,
                      val oem: String)
