package cm.aptoide.skills.factory

import cm.aptoide.skills.api.TicketApi
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TicketApiFactory {
  companion object {
    const val ENDPOINT = "https://6e2f9953-5363-45cb-a689-e2edfbe12f1e.mock.pstmn.io"
    fun providesTicketApi(client: OkHttpClient,
                          gson: Gson): TicketApi {
      val api = Retrofit.Builder()
          .baseUrl(ENDPOINT)
          .client(client)
          .addConverterFactory(GsonConverterFactory.create(gson))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build()
          .create(TicketApi::class.java)
      return api
    }
  }

}