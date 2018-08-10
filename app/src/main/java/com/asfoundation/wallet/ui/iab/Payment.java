package com.asfoundation.wallet.ui.iab;

import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;
import org.kethereum.erc681.ERC681ParserKt;
import org.spongycastle.util.encoders.Hex;

public class Payment {
  private final Status status;
  private final String uri;
  private @Nullable final String fromAddress;
  private @Nullable final String buyHash;
  private @Nullable final String packageName;
  private @Nullable final String productName;

  public Payment(String uri, Status status, @Nullable String fromAddress, @Nullable String buyHash,
      @Nullable String packageName, @Nullable String productName) {
    this.status = status;
    this.uri = uri;
    this.fromAddress = fromAddress;
    this.buyHash = buyHash;
    this.packageName = packageName;
    this.productName = productName;
  }

  public Payment(String uri, Status status) {
    this.uri = uri;
    this.status = status;
    this.fromAddress = null;
    this.buyHash = null;
    this.packageName = null;
    this.productName = null;
  }

  @Nullable public String getFromAddress() {
    return fromAddress;
  }

  public Status getStatus() {
    return status;
  }

  public String getUri() {
    return uri;
  }

  public @Nullable String getBuyHash() {
    return buyHash;
  }

  public @Nullable String getPackageName() {
    return packageName;
  }

  public @Nullable String getProductName() {
    return productName;
  }

  public @Nullable String getProductId() {
    try {
      return new String(Hex.decode(ERC681ParserKt.parseERC681(uri).getFunctionParams()
          .get("data")
          .substring(2)
          .getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }



  public enum Status {
    COMPLETED, NO_FUNDS, NETWORK_ERROR, NO_ETHER, NO_TOKENS, NO_INTERNET, NONCE_ERROR, APPROVING,
    BUYING, ERROR
  }

  @Override public String toString() {
    return "Payment{"
        + "status="
        + status
        + ", uri='"
        + uri
        + '\''
        + ", fromAddress='"
        + fromAddress
        + '\''
        + ", buyHash='"
        + buyHash
        + '\''
        + ", packageName='"
        + packageName
        + '\''
        + ", productName='"
        + productName
        + '\''
        + '}';
  }
}

