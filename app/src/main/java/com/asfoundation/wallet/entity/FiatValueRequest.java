package com.asfoundation.wallet.entity;

/**
 * Created by franciscocalado on 24/07/2018.
 */

public class FiatValueRequest {
  private double appc;

  public FiatValueRequest(double appcValue) {
    this.appc = appcValue;
  }

  public double getAppc() {
    return appc;
  }

  public void setAppc(double appc) {
    this.appc = appc;
  }
}
