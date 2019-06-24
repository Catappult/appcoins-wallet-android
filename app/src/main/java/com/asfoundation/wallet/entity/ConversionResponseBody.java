package com.asfoundation.wallet.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class ConversionResponseBody {

  private String currency;
  @JsonProperty("value") private BigDecimal appcValue;
  private String label;
  @JsonProperty("sign") private String symbol;

  public String getCurrency() {
    return currency;
  }

  public BigDecimal getAppcValue() {
    return appcValue;
  }

  public String getLabel() {
    return label;
  }

  public String getSymbol() {
    return symbol;
  }
}