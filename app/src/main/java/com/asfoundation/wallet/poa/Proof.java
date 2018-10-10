package com.asfoundation.wallet.poa;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class Proof {
  private final String packageName;
  private final String walletPackage;
  private final List<ProofComponent> proofComponentList;
  private final ProofStatus proofStatus;
  private final int chainId;
  private final BigDecimal gasPrice;
  private final BigDecimal gasLimit;
  @Nullable private final String countryCode;
  @Nullable private final String campaignId;
  @Nullable private final String oemAddress;
  @Nullable private final String storeAddress;
  @Nullable private final String hash;

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList, String walletPackage, ProofStatus proofStatus,
      int chainId, @Nullable String oemAddress, @Nullable String storeAddress, BigDecimal gasPrice,
      BigDecimal gasLimit, @Nullable String hash, @Nullable String countryCode) {
    this.packageName = packageName;
    this.campaignId = campaignId;
    this.proofComponentList = proofComponentList;
    this.walletPackage = walletPackage;
    this.proofStatus = proofStatus;
    this.chainId = chainId;
    this.oemAddress = oemAddress;
    this.storeAddress = storeAddress;
    this.gasPrice = gasPrice;
    this.gasLimit = gasLimit;
    this.hash = hash;
    this.countryCode = countryCode;
  }

  public Proof(String packageName, @Nullable String campaignId,
      List<ProofComponent> proofComponentList, String walletPackage, ProofStatus proofStatus,
      int chainId, @Nullable String oemAddress, @Nullable String storeAddress, BigDecimal gasPrice,
      BigDecimal gasLimit, String countryCode) {
    this(packageName, campaignId, proofComponentList, walletPackage, proofStatus, chainId,
        oemAddress, storeAddress, gasPrice, gasLimit, null, countryCode);
  }

  public Proof(String packageName, String walletPackage, ProofStatus proofStatus, int chainId) {
    this(packageName, null, Collections.emptyList(), walletPackage, proofStatus, chainId, null,
        null, BigDecimal.ZERO, BigDecimal.ZERO, null, null);
  }

  @Nullable public String getCountryCode() {
    return countryCode;
  }

  @Nullable public String getHash() {
    return hash;
  }

  public BigDecimal getGasPrice() {
    return gasPrice;
  }

  public BigDecimal getGasLimit() {
    return gasLimit;
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
    result = 31 * result + proofComponentList.hashCode();
    result = 31 * result + proofStatus.hashCode();
    result = 31 * result + chainId;
    result = 31 * result + (gasPrice != null ? gasPrice.hashCode() : 0);
    result = 31 * result + (gasLimit != null ? gasLimit.hashCode() : 0);
    result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
    result = 31 * result + (campaignId != null ? campaignId.hashCode() : 0);
    result = 31 * result + (oemAddress != null ? oemAddress.hashCode() : 0);
    result = 31 * result + (storeAddress != null ? storeAddress.hashCode() : 0);
    result = 31 * result + (hash != null ? hash.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Proof)) return false;

    Proof proof = (Proof) o;

    if (chainId != proof.chainId) return false;
    if (!packageName.equals(proof.packageName)) return false;
    if (!walletPackage.equals(proof.walletPackage)) return false;
    if (!proofComponentList.equals(proof.proofComponentList)) return false;
    if (proofStatus != proof.proofStatus) return false;
    if (gasPrice != null ? !gasPrice.equals(proof.gasPrice) : proof.gasPrice != null) return false;
    if (gasLimit != null ? !gasLimit.equals(proof.gasLimit) : proof.gasLimit != null) return false;
    if (countryCode != null ? !countryCode.equals(proof.countryCode) : proof.countryCode != null) {
      return false;
    }
    if (campaignId != null ? !campaignId.equals(proof.campaignId) : proof.campaignId != null) {
      return false;
    }
    if (oemAddress != null ? !oemAddress.equals(proof.oemAddress) : proof.oemAddress != null) {
      return false;
    }
    if (storeAddress != null ? !storeAddress.equals(proof.storeAddress)
        : proof.storeAddress != null) {
      return false;
    }
    return hash != null ? hash.equals(proof.hash) : proof.hash == null;
  }

  @Override public String toString() {
    return "Proof{"
        + "packageName='"
        + packageName
        + '\''
        + ", walletPackage='"
        + walletPackage
        + '\''
        + ", proofComponentList="
        + proofComponentList
        + ", proofStatus="
        + proofStatus
        + ", chainId="
        + chainId
        + ", gasPrice="
        + gasPrice
        + ", gasLimit=" + gasLimit + ", countryCode='" + countryCode + '\''
        + ", campaignId='"
        + campaignId
        + '\''
        + ", oemAddress='"
        + oemAddress
        + '\''
        + ", storeAddress='" + storeAddress + '\'' + ", hash='" + hash
        + '\''
        + '}';
  }
}
