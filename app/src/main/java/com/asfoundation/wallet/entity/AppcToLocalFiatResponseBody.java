package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL) @JsonPropertyOrder({
    "currency", "value"
}) public class AppcToLocalFiatResponseBody {

  @JsonProperty("currency") private String currency;
  @JsonProperty("value") private Double appcValue;

  @JsonProperty("currency") public String getCurrency() {
    return currency;
  }

  @JsonProperty("currency") public void setCurrency(String currency) {
    this.currency = currency;
  }

  @JsonProperty("value") public Double getAppcValue() {
    return appcValue;
  }

  @JsonProperty("value") public void setAppcValue(Double appcValue) {
    this.appcValue = appcValue;
  }
}