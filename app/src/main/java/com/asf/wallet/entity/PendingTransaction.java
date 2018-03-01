package com.asf.wallet.entity;

/**
 * Created by trinkes on 28/02/2018.
 */

public class PendingTransaction {
  private final String hash;
  private final Boolean pending;

  public PendingTransaction(String hash, Boolean pending) {

    this.hash = hash;
    this.pending = pending;
  }

  public String getHash() {
    return hash;
  }

  public Boolean isPending() {
    return pending;
  }

  @Override public String toString() {
    return "PendingTransaction{" + "hash='" + hash + '\'' + ", pending=" + pending + '}';
  }
}
