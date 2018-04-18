package com.asfoundation.wallet.poa;

public class ProofComponent {
  private final long timeStamp;
  private final long nonce;

  public ProofComponent(long timeStamp, long nonce) {
    this.timeStamp = timeStamp;
    this.nonce = nonce;
  }

  @Override public int hashCode() {
    int result = (int) (timeStamp ^ (timeStamp >>> 32));
    result = 31 * result + (int) (nonce ^ (nonce >>> 32));
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProofComponent)) return false;

    ProofComponent that = (ProofComponent) o;

    if (timeStamp != that.timeStamp) return false;
    return nonce == that.nonce;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public long getNonce() {
    return nonce;
  }
}
