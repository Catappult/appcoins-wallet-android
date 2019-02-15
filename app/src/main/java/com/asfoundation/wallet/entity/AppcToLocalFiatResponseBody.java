package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppcToLocalFiatResponseBody {

  private String currency;
  @JsonProperty("value") private Double appcValue;

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Double getAppcValue() {
    return appcValue;
  }

  public void setAppcValue(Double appcValue) {
    this.appcValue = appcValue;
  }
}