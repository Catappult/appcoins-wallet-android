package com.asfoundation.wallet.service

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.SubmitPoAException
import com.asfoundation.wallet.entity.SubmitPoAResponse
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofComponent
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.NotNull
import retrofit2.http.*

class CampaignService(
    private val campaignApi: @NotNull CampaignApi,
    private val versionCode: Int) {

  companion object {
    const val SERVICE_HOST = BuildConfig.BACKEND_HOST
  }

  fun submitProof(proof: Proof, wallet: String): Single<String> {
    return campaignApi.submitProof(
        SerializedProof(proof.campaignId, proof.packageName, wallet, proof.proofComponentList,
            proof.storeAddress, proof.oemAddress), versionCode)
        .map { response -> handleResponse(response) }
        .subscribeOn(Schedulers.io())
        .singleOrError()
  }

  fun getCampaign(address: String, packageName: String, packageVersionCode: Int): Single<String> {
    return campaignApi.getCampaign(address, packageName, packageVersionCode)
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

  private fun handleResponse(response: GetCampaignResponse): String {
    return if (response.status != GetCampaignResponse.EligibleResponseStatus.NOT_ELIGIBLE
        && response.bidId != null) {
      response.bidId
    } else {
      Campaign.NOT_ELIGIBLE.toString()
    }
  }


  interface CampaignApi {
    @Headers("Content-Type: application/json")
    @POST("/campaign/submitpoa")
    fun submitProof(@Body body: SerializedProof?, @Query("version_code")
    versionCode: Int): Observable<SubmitPoAResponse>


    @GET("/campaign/eligible")
    fun getCampaign(@Query("address") address: String,
                    @Query("package_name") packageName: String,
                    @Query("vercode") versionCode: Int): Observable<GetCampaignResponse>
  }
}

class SerializedProof(val bid_id: String?,
                      val package_name: String,
                      val address: String,
                      val nonces: List<ProofComponent>,
                      val store: String,
                      val oem: String)

enum class Campaign {
  NOT_ELIGIBLE
}
