package com.asfoundation.wallet.service

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.entity.SubmitPoAException
import com.asfoundation.wallet.entity.SubmitPoAResponse
import com.asfoundation.wallet.poa.PoaInformationModel
import com.asfoundation.wallet.poa.Proof
import com.asfoundation.wallet.poa.ProofComponent
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Named

class CampaignService @Inject constructor(private val campaignApi: CampaignApi,
                                          @Named("local_version_code") private val versionCode: Int,
                                          private val rxSchedulers: RxSchedulers) {

  fun submitProof(proof: Proof, wallet: String): Single<String> {
    return campaignApi.submitProof(
        SerializedProof(proof.campaignId, proof.packageName, wallet, proof.proofComponentList,
            proof.storeAddress, proof.oemAddress), versionCode)
        .map { response -> handleResponse(response) }
        .subscribeOn(rxSchedulers.io)
        .singleOrError()
  }

  fun getCampaign(address: String, packageName: String,
                  packageVersionCode: Int): Single<Campaign> {
    return campaignApi.getCampaign(address, packageName, packageVersionCode)
        .map { response -> handleResponse(response) }
        .subscribeOn(rxSchedulers.io)
        .singleOrError()
  }

  fun retrievePoaInformation(address: String): Single<PoaInformationModel> {
    return campaignApi.getPoaInformation(address)
        .map { handleResponse(it) }
        .subscribeOn(rxSchedulers.io)
        .singleOrError()
  }

  private fun handleResponse(response: PoaInformationResponse): PoaInformationModel {
    return PoaInformationModel(response.remainingPoa, response.hoursRemaining,
        response.minutesRemaining)
  }

  private fun handleResponse(response: SubmitPoAResponse): String {
    if (response.isValid) {
      return response.transactionId
    } else {
      throw SubmitPoAException(response.errorCode)
    }
  }

  private fun handleResponse(response: GetCampaignResponse): Campaign {
    return if (response.status != GetCampaignResponse.EligibleResponseStatus.NOT_ELIGIBLE && response.bidId != null) {
      Campaign(response.bidId, CampaignStatus.AVAILABLE)
    } else {
      Campaign("", CampaignStatus.NOT_ELIGIBLE, response.hours, response.minutes)
    }
  }

  interface CampaignApi {
    @Headers("Content-Type: application/json")
    @POST("/campaign/submitpoa")
    fun submitProof(@Body body: SerializedProof?, @Query("version_code")
    versionCode: Int): Observable<SubmitPoAResponse>

    @GET("/campaign/remaining_poa")
    fun getPoaInformation(@Query("address") address: String): Observable<PoaInformationResponse>

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

enum class CampaignStatus {
  AVAILABLE, NOT_ELIGIBLE
}

data class Campaign(val campaignId: String, val campaignStatus: CampaignStatus,
                    val hoursRemaining: Int = 0, val minutesRemaining: Int = 0)
