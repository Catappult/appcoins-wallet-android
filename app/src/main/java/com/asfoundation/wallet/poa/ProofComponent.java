package com.asfoundation.wallet.poa;

import javax.annotation.Nullable;

public class ProofComponent {
  private final long timeStamp;
  @Nullable private final String data;

  public ProofComponent(long timeStamp, @Nullable String data) {
    this.timeStamp = timeStamp;
    this.data = data;
  }

  @Override public int hashCode() {
    int result = (int) (timeStamp ^ (timeStamp >>> 32));
    result = 31 * result + (data != null ? data.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProofComponent)) return false;

    ProofComponent that = (ProofComponent) o;

    if (timeStamp != that.timeStamp) return false;
    return data != null ? data.equals(that.data) : that.data == null;
  }
}
