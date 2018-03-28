package com.asfoundation.wallet.poa;

public class Proof {
  private final String packageName;
  private final String campaignId;

  public Proof(String packageName, String campaignId) {
    this.packageName = packageName;
    this.campaignId = campaignId;
  }

  @Override public int hashCode() {
    int result = packageName != null ? packageName.hashCode() : 0;
    result = 31 * result + (campaignId != null ? campaignId.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (packageName != null ? !packageName.equals(proof.packageName) : proof.packageName != null) {
      return false;
    }
    return campaignId != null ? campaignId.equals(proof.campaignId) : proof.campaignId == null;
  }
}
