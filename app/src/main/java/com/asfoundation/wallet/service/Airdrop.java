package com.asfoundation.wallet.service;

import javax.annotation.Nullable;

public class Airdrop {
  private final AirdropStatus status;
  private final @Nullable String errorMessage;

  public Airdrop(AirdropStatus status, @Nullable String errorMessage) {
    this.status = status;
    this.errorMessage = errorMessage;
  }

  public Airdrop(AirdropStatus status) {
    this.status = status;
    this.errorMessage = null;
  }

  public AirdropStatus getStatus() {
    return status;
  }

  @Nullable public String getErrorMessage() {
    return errorMessage;
  }

  public enum AirdropStatus {
    PENDING, ERROR, API_ERROR, SUCCESS, EMPTY
  }
}
