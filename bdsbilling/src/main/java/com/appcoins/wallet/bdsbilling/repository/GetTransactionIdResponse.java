package com.appcoins.wallet.bdsbilling.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) public class GetTransactionIdResponse {

  @JsonProperty("txid") private String txid;
  @JsonProperty("status") private String status;

  @JsonProperty("txid") public String getTxid() {
    return txid;
  }

  @JsonProperty("txid") public void setTxid(String txid) {
    this.txid = txid;
  }

  @JsonProperty("status") public String getStatus() {
    return status;
  }

  @JsonProperty("status") public void setStatus(String status) {
    this.status = status;
  }
}