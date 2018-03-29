package com.asfoundation.wallet.poa;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class Proof {
  private final String packageName;
  @Nullable private final String proofId;
  private final String walletPackage;
  @Nullable private final String campaignId;
  private final List<ProofComponent> proofComponentList;

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList, @Nullable String proofId, String walletPackage) {
    this.packageName = packageName;
    this.campaignId = campaignId;
    this.proofComponentList = proofComponentList;
    this.proofId = proofId;
    this.walletPackage = walletPackage;
  }

  public Proof(String packageName, String walletPackage) {
    this(packageName, null, Collections.emptyList(), null, walletPackage);
  }

  public String getWalletPackage() {
    return walletPackage;
  }

  @Nullable public String getProofId() {
    return proofId;
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
    result = 31 * result + (proofId != null ? proofId.hashCode() : 0);
    result = 31 * result + walletPackage.hashCode();
    result = 31 * result + (campaignId != null ? campaignId.hashCode() : 0);
    result = 31 * result + proofComponentList.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (!packageName.equals(proof.packageName)) return false;
    if (proofId != null ? !proofId.equals(proof.proofId) : proof.proofId != null) return false;
    if (!walletPackage.equals(proof.walletPackage)) return false;
    if (campaignId != null ? !campaignId.equals(proof.campaignId) : proof.campaignId != null) {
      return false;
    }
    return proofComponentList.equals(proof.proofComponentList);
  }
}
