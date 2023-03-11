package com.appcoins.wallet.bdsbilling.repository.entity.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Authorization {

  @JsonProperty("uid") private String uid;
  @JsonProperty("type") private String type;
  @JsonProperty("status") private String status;
  @JsonProperty("data") private Data data;

  @JsonProperty("uid") public String getUid() {
    return uid;
  }

  @JsonProperty("uid") public void setUid(String uid) {
    this.uid = uid;
  }

  @JsonProperty("type") public String getType() {
    return type;
  }

  @JsonProperty("type") public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("status") public String getStatus() {
    return status;
  }

  @JsonProperty("status") public void setStatus(String status) {
    this.status = status;
  }

  @JsonProperty("data") public Data getData() {
    return data;
  }

  @JsonProperty("data") public void setData(Data data) {
    this.data = data;
  }
}
