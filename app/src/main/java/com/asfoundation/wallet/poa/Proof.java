package com.asfoundation.wallet.poa;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class Proof {
  private final String packageName;
  @Nullable private final String campaignId;
  private final List<ProofComponent> proofComponentList;

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList) {
    this.packageName = packageName;
    this.campaignId = campaignId;
    this.proofComponentList = proofComponentList;
  }

  public Proof(String packageName, String campaignId) {
    this(packageName, campaignId, Collections.emptyList());
  }

  public Proof(String packageName) {
    this(packageName, null);
  }

  public List<ProofComponent> getProofComponentList() {
    return Collections.unmodifiableList(proofComponentList);
  }

  public String getPackageName() {
    return packageName;
  }

  public @Nullable String getCampaignId() {
    return campaignId;
  }

  @Override public int hashCode() {
    int result = packageName.hashCode();
    result = 31 * result + (campaignId != null ? campaignId.hashCode() : 0);
    result = 31 * result + proofComponentList.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (!packageName.equals(proof.packageName)) return false;
    if (campaignId != null ? !campaignId.equals(proof.campaignId) : proof.campaignId != null) {
      return false;
    }
    return proofComponentList.equals(proof.proofComponentList);
  }
}
