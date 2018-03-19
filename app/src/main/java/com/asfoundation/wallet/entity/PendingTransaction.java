package com.asfoundation.wallet.entity;

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

    if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
    return pending != null ? pending.equals(that.pending) : that.pending == null;
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
