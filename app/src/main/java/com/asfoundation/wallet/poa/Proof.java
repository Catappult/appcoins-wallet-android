package com.asfoundation.wallet.poa;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class Proof {
  private final String packageName;
  private final String walletPackage;
  @Nullable private final String campaignId;
  private final List<ProofComponent> proofComponentList;
  private final ProofStatus proofStatus;
  private final int chainId;
  @Nullable private final String oemAddress;
  @Nullable private final String storeAddress;

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList, String walletPackage, ProofStatus proofStatus,
      int chainId, @Nullable String oemAddress,
      @Nullable String storeAddress) {
    this.packageName = packageName;
    this.campaignId = campaignId;
    this.proofComponentList = proofComponentList;
    this.walletPackage = walletPackage;
    this.proofStatus = proofStatus;
    this.chainId = chainId;
    this.oemAddress = oemAddress;
    this.storeAddress = storeAddress;
  }

  public Proof(String packageName, String walletPackage, ProofStatus proofStatus, int chainId) {
    this(packageName, null, Collections.emptyList(), walletPackage, proofStatus, chainId,
        null, null);
  }

  public String getOemAddress() {
    return oemAddress;
  }

  public String getStoreAddress() {
    return storeAddress;
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
    result = 31 * result + walletPackage.hashCode();
    result = 31 * result + (campaignId != null ? campaignId.hashCode() : 0);
    result = 31 * result + proofComponentList.hashCode();
    result = 31 * result + proofStatus.hashCode();
    result = 31 * result + chainId;
    result = 31 * result + (oemAddress != null ? oemAddress.hashCode() : 0);
    result = 31 * result + (storeAddress != null ? storeAddress.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (chainId != proof.chainId) return false;
    if (!packageName.equals(proof.packageName)) return false;
    if (!walletPackage.equals(proof.walletPackage)) return false;
    if (campaignId != null ? !campaignId.equals(proof.campaignId) : proof.campaignId != null) {
      return false;
    }
    if (!proofComponentList.equals(proof.proofComponentList)) return false;
    if (proofStatus != proof.proofStatus) return false;
    if (oemAddress != null ? !oemAddress.equals(proof.oemAddress) : proof.oemAddress != null) {
      return false;
    }
    return storeAddress != null ? storeAddress.equals(proof.storeAddress)
        : proof.storeAddress == null;
  }

  @Override public String toString() {
    return "Proof{"
        + "packageName='"
        + packageName
        + '\''
        + ", proofStatus="
        + proofStatus
        + '\''
        + ", walletPackage='"
        + walletPackage
        + '\''
        + ", campaignId='"
        + campaignId
        + '\'' + ", chainId='" + chainId + '\''
        + ", proofComponentList="
        + proofComponentList
        + '}';
  }
}
