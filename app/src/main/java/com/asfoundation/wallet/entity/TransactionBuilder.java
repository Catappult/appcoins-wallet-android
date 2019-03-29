package com.asfoundation.wallet.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.asfoundation.wallet.repository.TokenRepository;
import com.asfoundation.wallet.util.BalanceUtils;
import io.reactivex.annotations.NonNull;
import java.math.BigDecimal;
import java.util.Arrays;

import static com.asfoundation.wallet.C.ETHER_DECIMALS;

public class TransactionBuilder implements Parcelable {
  public static final long NO_CHAIN_ID = -1;
  public static final Creator<TransactionBuilder> CREATOR = new Creator<TransactionBuilder>() {
    @Override public TransactionBuilder createFromParcel(Parcel in) {
      return new TransactionBuilder(in);
    }

    @Override public TransactionBuilder[] newArray(int size) {
      return new TransactionBuilder[size];
    }
  };
  private String contractAddress;
  private int decimals;
  private String symbol;
  private boolean shouldSendToken;
  private String toAddress;
  private String fromAddress;
  private BigDecimal amount = BigDecimal.ZERO;
  private byte[] data;
  private byte[] appcoinsData;
  private GasSettings gasSettings;
  private long chainId;
  private String skuId;
  private String type;
  private String origin;
  private String domain;
  private String payload;
  private String iabContract;
  private String callbackUrl;
  private String orderReference;
  private String originalOneStepValue;
  private String originalOneStepCurrency;

  public TransactionBuilder(@NonNull TokenInfo tokenInfo) {
    contractAddress(tokenInfo.address).decimals(tokenInfo.decimals)
        .symbol(tokenInfo.symbol)
        .shouldSendToken(!tokenInfo.symbol.equalsIgnoreCase("ETH"));
    chainId = NO_CHAIN_ID;
  }

  public TransactionBuilder(@NonNull String symbol) {
    symbol(symbol).decimals(ETHER_DECIMALS);
    chainId = NO_CHAIN_ID;
  }

  private TransactionBuilder(Parcel in) {
    contractAddress = in.readString();
    decimals = in.readInt();
    symbol = in.readString();
    shouldSendToken = in.readInt() == 1;
    toAddress = in.readString();
    fromAddress = in.readString();
    amount = new BigDecimal(in.readString());
    data = in.createByteArray();
    gasSettings = in.readParcelable(GasSettings.class.getClassLoader());
    chainId = in.readLong();
    skuId = in.readString();
    type = in.readString();
    origin = in.readString();
    domain = in.readString();
    payload = in.readString();
    callbackUrl = in.readString();
    orderReference = in.readString();
    originalOneStepValue = in.readString();
    originalOneStepCurrency = in.readString();
  }

  public TransactionBuilder(String symbol, String contractAddress, Long chainId, String toAddress,
      BigDecimal amount, String skuId, int decimals, String type, String origin, String domain,
      String payload, String callbackUrl, String orderReference) {
    this.symbol = symbol;
    this.contractAddress = contractAddress;
    this.chainId = chainId == null ? NO_CHAIN_ID : chainId;
    this.toAddress = toAddress;
    this.amount = amount;
    this.skuId = skuId;
    this.shouldSendToken = false;
    this.decimals = decimals;
    this.type = type;
    this.origin = origin;
    this.domain = domain;
    this.payload = payload;
    this.callbackUrl = callbackUrl;
    this.orderReference = orderReference;
  }

  public TransactionBuilder(String symbol, String contractAddress, Long chainId,
      String receiverAddress, BigDecimal tokenTransferAmount, String skuId, int decimals,
      String iabContract, String type, String origin, String domain, String payload,
      String callbackUrl, String orderReference) {
    this(symbol, contractAddress, chainId, receiverAddress, tokenTransferAmount, skuId, decimals,
        type, origin, domain, payload, callbackUrl, orderReference);
    this.iabContract = iabContract;
  }

  public TransactionBuilder(String symbol, String contractAddress, Long chainId,
      String receiverAddress, BigDecimal tokenTransferAmount, int decimals) {
    this(symbol, contractAddress, chainId, receiverAddress, tokenTransferAmount, "", decimals, "",
        null, "", "", "", "");
  }

  public String getIabContract() {
    return iabContract;
  }

  public long getChainId() {
    return chainId;
  }

  public TransactionBuilder symbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  public String symbol() {
    return symbol;
  }

  public TransactionBuilder contractAddress(String address) {
    this.contractAddress = address;
    return this;
  }

  public String contractAddress() {
    return contractAddress;
  }

  public TransactionBuilder decimals(int decimals) {
    this.decimals = decimals;
    return this;
  }

  public int decimals() {
    return decimals;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public TransactionBuilder shouldSendToken(boolean shouldSendToken) {
    this.shouldSendToken = shouldSendToken;
    return this;
  }

  public boolean shouldSendToken() {
    return shouldSendToken;
  }

  public TransactionBuilder toAddress(String toAddress) {
    this.toAddress = toAddress;
    return this;
  }

  public String toAddress() {
    return toAddress;
  }

  public TransactionBuilder amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  public BigDecimal amount() {
    return amount;
  }

  public BigDecimal subunitAmount() {
    return BalanceUtils.baseToSubunit(amount, decimals);
  }

  public String getSkuId() {
    return skuId;
  }

  public void setSkuId(String skuId) {
    this.skuId = skuId;
  }

  public TransactionBuilder data(byte[] data) {
    this.data = data;
    return this;
  }

  public byte[] data() {
    if (shouldSendToken) {
      return TokenRepository.createTokenTransferData(toAddress, subunitAmount());
    } else {
      return data;
    }
  }

  public TransactionBuilder appcoinsData(byte[] appcoinsData) {
    this.appcoinsData = appcoinsData;
    return this;
  }

  public byte[] appcoinsData() {
    return appcoinsData;
  }

  public TransactionBuilder gasSettings(GasSettings gasSettings) {
    this.gasSettings = gasSettings;
    return this;
  }

  public GasSettings gasSettings() {
    return gasSettings;
  }

  public TransactionBuilder fromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
    return this;
  }

  public String fromAddress() {
    return fromAddress;
  }

  public String getType() {
    return type;
  }

  public String getOrigin() {
    return origin;
  }

  public String getDomain() {
    return domain;
  }

  public String getPayload() {
    return payload;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public String getOriginalOneStepValue() {
    return originalOneStepValue;
  }

  public void setOriginalOneStepValue(String originalOneStepValue) {
    this.originalOneStepValue = originalOneStepValue;
  }

  public String getOriginalOneStepCurrency() {
    return originalOneStepCurrency;
  }

  public void setOriginalOneStepCurrency(String originalOneStepCurrency) {
    this.originalOneStepCurrency = originalOneStepCurrency;
  }

  @Override public String toString() {
    return "TransactionBuilder{"
        + "contractAddress='"
        + contractAddress
        + '\''
        + ", decimals="
        + decimals
        + ", symbol='"
        + symbol
        + '\''
        + ", shouldSendToken="
        + shouldSendToken
        + ", toAddress='"
        + toAddress
        + '\''
        + ", fromAddress='"
        + fromAddress
        + '\''
        + ", amount="
        + amount
        + ", data="
        + Arrays.toString(data)
        + ", appcoinsData="
        + Arrays.toString(appcoinsData)
        + ", gasSettings="
        + gasSettings
        + ", chainId="
        + chainId
        + ", skuId='"
        + skuId
        + '\''
        + ", type='"
        + type
        + '\''
        + ", domain='"
        + domain
        + '\''
        + ", payload='"
        + payload
        + '\''
        + ", callbackUrl='"
        + callbackUrl
        + '\''
        + ", iabContract='"
        + iabContract
        + '\''
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(contractAddress);
    dest.writeInt(decimals);
    dest.writeString(symbol);
    dest.writeInt(shouldSendToken ? 1 : 0);
    dest.writeString(toAddress);
    dest.writeString(fromAddress);
    dest.writeString(amount.toString());
    dest.writeByteArray(data);
    dest.writeParcelable(gasSettings, flags);
    dest.writeLong(chainId);
    dest.writeString(skuId);
    dest.writeString(type);
    dest.writeString(origin);
    dest.writeString(domain);
    dest.writeString(payload);
    dest.writeString(callbackUrl);
    dest.writeString(orderReference);
    dest.writeString(originalOneStepValue);
    dest.writeString(originalOneStepCurrency);
  }

  public byte[] approveData() {
    BigDecimal base = new BigDecimal("10");
    return TokenRepository.createTokenApproveData(iabContract, amount.multiply(base.pow(decimals)));
  }

  public String getOrderReference() {
    return orderReference;
  }
}
