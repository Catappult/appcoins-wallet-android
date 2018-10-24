package com.appcoins.wallet.bdsbilling.repository.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "orderId", "packageName", "productId", "purchaseTime", "purchaseToken", "purchaseState",
    "developerPayload"
}) @JsonInclude(JsonInclude.Include.NON_NULL) public class DeveloperPurchase {
  @JsonProperty("orderId") private String orderId;
  @JsonProperty("packageName") private String packageName;
  @JsonProperty("productId") private String productId;
  @JsonProperty("purchaseTime") private long purchaseTime;
  @JsonProperty("purchaseToken") private String purchaseToken;
  @JsonProperty("purchaseState") private int purchaseState;
  @JsonProperty("developerPayload") private String developerPayload;

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public long getPurchaseTime() {
    return purchaseTime;
  }

  public void setPurchaseTime(long purchaseTime) {
    this.purchaseTime = purchaseTime;
  }

  public String getPurchaseToken() {
    return purchaseToken;
  }

  public void setPurchaseToken(String purchaseToken) {
    this.purchaseToken = purchaseToken;
  }

  public int getPurchaseState() {
    return purchaseState;
  }

  public void setPurchaseState(int purchaseState) {
    this.purchaseState = purchaseState;
  }

  public String getDeveloperPayload() {
    return developerPayload;
  }

  public void setDeveloperPayload(String developerPayload) {
    this.developerPayload = developerPayload;
  }
}
