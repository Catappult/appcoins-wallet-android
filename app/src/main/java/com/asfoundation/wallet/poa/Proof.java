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
  private final ProofStatus proofStatus;
  private final int chainId;

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList, @Nullable String proofId, String walletPackage,
      ProofStatus proofStatus, int chainId) {
    this.packageName = packageName;
    this.campaignId = campaignId;
    this.proofComponentList = proofComponentList;
    this.proofId = proofId;
    this.walletPackage = walletPackage;
    this.proofStatus = proofStatus;
    this.chainId = chainId;
  }

  public Proof(String packageName, String walletPackage, ProofStatus proofStatus, int chainId) {
    this(packageName, null, Collections.emptyList(), null, walletPackage, proofStatus, chainId);
  }

  public int getChainId() {
    return chainId;
  }

  public ProofStatus getProofStatus() {
    return proofStatus;
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
    result = 31 * result + proofStatus.hashCode();
    result = 31 * result + chainId;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (chainId != proof.chainId) return false;
    if (!packageName.equals(proof.packageName)) return false;
    if (proofId != null ? !proofId.equals(proof.proofId) : proof.proofId != null) return false;
    if (!walletPackage.equals(proof.walletPackage)) return false;
    if (campaignId != null ? !campaignId.equals(proof.campaignId) : proof.campaignId != null) {
      return false;
    }
    if (!proofComponentList.equals(proof.proofComponentList)) return false;
    return proofStatus == proof.proofStatus;
  }

  @Override public String toString() {
    return "Proof{"
        + "packageName='"
        + packageName
        + '\''
        + ", proofStatus="
        + proofStatus
        + ", proofId='"
        + proofId
        + '\''
        + ", walletPackage='"
        + walletPackage
        + '\''
        + ", campaignId='"
        + campaignId
        + '\''
        + ", proofComponentList="
        + proofComponentList
        + '}';
  }
}
