package com.asfoundation.wallet;

import org.jetbrains.annotations.Nullable;

public class AirdropData {
  public static final int UNDEFINED = -1;
  private final AirdropStatus status;
  private final @Nullable String message;
  private final int networkId;

  public AirdropData(AirdropStatus status, @Nullable String message, int networkId) {
    this.status = status;
    this.message = message;
    this.networkId = networkId;
  }

  public AirdropData(AirdropStatus status) {
    this.status = status;
    this.message = null;
    networkId = UNDEFINED;
  }

  public AirdropData(AirdropStatus status, @Nullable String message) {
    this.status = status;
    this.message = message;
    networkId = UNDEFINED;
  }

  public int getNetworkId() {
    return networkId;
  }

  @Override public int hashCode() {
    int result = status.hashCode();
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + networkId;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AirdropData)) return false;

    AirdropData airdrop = (AirdropData) o;

    if (networkId != airdrop.networkId) return false;
    if (status != airdrop.status) return false;
    return message != null ? message.equals(airdrop.message) : airdrop.message == null;
  }

  @Override public String toString() {
    return "Airdrop{"
        + "status="
        + status
        + ", message='"
        + message
        + '\''
        + ", networkId="
        + networkId
        + '}';
  }

  public AirdropStatus getStatus() {
    return status;
  }

  @Nullable public String getMessage() {
    return message;
  }

  public enum AirdropStatus {
    PENDING, ERROR, API_ERROR, SUCCESS, CAPTCHA_ERROR, UNDEFINED
  }
}
