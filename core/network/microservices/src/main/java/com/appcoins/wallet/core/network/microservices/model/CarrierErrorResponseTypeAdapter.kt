package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class CarrierErrorResponseTypeAdapter : TypeAdapter<CarrierErrorResponse>() {
  private val gson: Gson = Gson()

  override fun write(jsonWriter: JsonWriter, error: CarrierErrorResponse) {
    gson.toJson(error, CarrierErrorResponse::class.java, jsonWriter)
  }

  // You can thank webservices for this abomination
  override fun read(reader: JsonReader): CarrierErrorResponse {
    var code: String? = null
    var path: String? = null
    var text: String? = null
    val data: ArrayList<CarrierErrorResponse.Data> = ArrayList()

    reader.beginObject();

    var fieldName: String? = null
    while (reader.hasNext()) {
      val token: JsonToken = reader.peek()
      if (token == JsonToken.NAME) {
        fieldName = reader.nextName()
      }
      when (fieldName) {
        "code" -> code = reader.nextString()
        "path" -> path = reader.nextString()
        "text" -> text = reader.nextString()
        "data" -> {
          val dataPeek = reader.peek()
          if (dataPeek == JsonToken.BEGIN_ARRAY) {
            val parsed: Array<CarrierErrorResponse.Data> =
                gson.fromJson(reader, Array<CarrierErrorResponse.Data>::class.java)
            data.addAll(parsed)
          } else if (dataPeek == JsonToken.BEGIN_OBJECT) {
            val parsed: CarrierErrorResponse.Data =
                gson.fromJson(reader, CarrierErrorResponse.Data::class.java)
            data.add(parsed)
          } else {
            throw JsonParseException("Unexpected token $dataPeek");
          }
        }
      }
    }

    reader.endObject();
    return CarrierErrorResponse(
      code,
      path,
      text,
      data
    )
  }
}