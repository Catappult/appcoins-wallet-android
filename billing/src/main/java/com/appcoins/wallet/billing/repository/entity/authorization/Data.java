package com.appcoins.wallet.billing.repository.entity.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {

  @JsonProperty("session") private String session;

  @JsonProperty("session") public String getSession() {
    return session;
  }

  @JsonProperty("session") public void setSession(String session) {
    this.session = session;
  }
}