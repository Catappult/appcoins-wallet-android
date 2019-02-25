package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConversionResponseBody {

  private String currency;
  @JsonProperty("value") private Double appcValue;
  private String label;
  @JsonProperty("sign") private String symbol;

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

  public String getLabel() {
    return label;
  }

  public String getSymbol() {
    return symbol;
  }
}