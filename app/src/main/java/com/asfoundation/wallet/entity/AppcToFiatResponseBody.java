package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL) @JsonPropertyOrder({
    "Datetime", "APPC"
}) public class AppcToFiatResponseBody {

  @JsonProperty("Datetime") private String datetime;
  @JsonProperty("APPC") private Double fiatValue;

  @JsonProperty("Datetime") public String getDatetime() {
    return datetime;
  }

  @JsonProperty("Datetime") public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  @JsonProperty("APPC") public Double getFiatValue() {
    return fiatValue;
  }

  @JsonProperty("APPC") public void setFiatValue(Double aPPC) {
    this.fiatValue = aPPC;
  }
}