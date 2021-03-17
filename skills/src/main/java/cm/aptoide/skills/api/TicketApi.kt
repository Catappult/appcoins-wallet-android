package cm.aptoide.skills.api

import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TicketApi {

  @POST("queue/ticket")
  fun postTicket(@Header("authorization") authorization: String,
                 @Body ticketRequest: TicketRequest): Single<TicketResponse>

}
