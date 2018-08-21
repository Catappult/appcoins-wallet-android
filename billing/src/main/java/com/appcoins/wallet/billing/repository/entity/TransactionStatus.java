package com.appcoins.wallet.billing.repository.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionStatus {

  @JsonProperty("uid") private String uid;

  @JsonProperty("status") private String status;

  @JsonProperty("uid") public String getUid() {
    return uid;
  }

  @JsonProperty("uid") public void setUid(String uid) {
    this.uid = uid;
  }

  @JsonProperty("status") public String getStatus() {
    return status;
  }

  @JsonProperty("status") public void setStatus(String status) {
    this.status = status;
  }
}