package com.asfoundation.wallet.entity;

import java.util.Objects;

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

  @Override public int hashCode() {
    int result = hash != null ? hash.hashCode() : 0;
    result = 31 * result + (pending != null ? pending.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PendingTransaction)) return false;

    PendingTransaction that = (PendingTransaction) o;

    if (!Objects.equals(hash, that.hash)) return false;
    return Objects.equals(pending, that.pending);
  }

  @Override public String toString() {
    return "PendingTransaction{" + "hash='" + hash + '\'' + ", pending=" + pending + '}';
  }

  public String getHash() {
    return hash;
  }

  public Boolean isPending() {
    return pending;
  }
}
