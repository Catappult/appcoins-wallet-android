package com.appcoins.wallet.core.network.eskills.api

import cm.aptoide.skills.model.EskillsVerification
import com.appcoins.wallet.core.network.eskills.model.TicketRequest
import com.appcoins.wallet.core.network.eskills.model.TicketResponse
import io.reactivex.Single
import retrofit2.http.*

interface TicketApi {

  @POST("queue/ticket/")
  fun postTicket(
    @Header("authorization") authorization: String,
    @Body ticketRequest: TicketRequest
  ): Single<TicketResponse>

  @GET("queue/ticket/{ticket_id}")
  fun getTicket(
    @Header("authorization") authorization: String,
    @Path("ticket_id") ticketId: String
  ): Single<TicketResponse>

  @GET("queue/eskills/service/verification")
  fun getVerification(
    @Header("authorization") authorization: String,
  ): Single<EskillsVerification>

  @PATCH("queue/ticket/{ticket_id}/status")
  fun cancelTicket(
    @Header("authorization") authorization: String,
    @Path("ticket_id") ticketId: String,
    @Body data: Refunded
  ): Single<TicketResponse>

  data class Refunded(val status: String = "REFUNDED")
}
