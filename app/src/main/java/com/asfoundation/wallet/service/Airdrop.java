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
