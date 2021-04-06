package cm.aptoide.skills.api

import cm.aptoide.skills.model.PayTicketRequest
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Single
import retrofit2.http.*

interface TicketApi {

  @POST("queue/ticket/")
  fun postTicket(@Header("authorization") authorization: String,
                 @Body ticketRequest: TicketRequest): Single<TicketResponse>

  @GET("queue/ticket/{ticket_id}")
  fun getTicket(@Header("authorization") authorization: String,
                @Path("ticket_id") ticketId: String): Single<TicketResponse>

  @POST("queue/dummy/purchase")
  fun createTicket(@Body payTicketRequest: PayTicketRequest): Single<Any>

}
