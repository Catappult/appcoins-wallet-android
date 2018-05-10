package com.asfoundation.wallet.service;

import javax.annotation.Nullable;

public class Airdrop {
  private final AirdropStatus status;
  private final @Nullable String message;

  public Airdrop(AirdropStatus status, @Nullable String message) {
    this.status = status;
    this.message = message;
  }

  public Airdrop(AirdropStatus status) {
    this.status = status;
    this.message = null;
  }

  @Override public int hashCode() {
    int result = status.hashCode();
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Airdrop)) return false;

    Airdrop airdrop = (Airdrop) o;

    if (status != airdrop.status) return false;
    return message != null ? message.equals(airdrop.message) : airdrop.message == null;
  }

  @Override public String toString() {
    return "Airdrop{" + "status=" + status + ", message='" + message + '\'' + '}';
  }

  public AirdropStatus getStatus() {
    return status;
  }

  @Nullable public String getMessage() {
    return message;
  }

  public enum AirdropStatus {
    PENDING, ERROR, API_ERROR, SUCCESS, EMPTY
  }
}
