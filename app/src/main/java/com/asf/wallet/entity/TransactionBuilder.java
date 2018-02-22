package com.asf.wallet.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.asf.wallet.repository.TokenRepository;
import com.asf.wallet.util.BalanceUtils;
import io.reactivex.annotations.NonNull;
import java.math.BigDecimal;
import java.util.Arrays;

import static com.asf.wallet.C.ETHER_DECIMALS;

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
  private GasSettings gasSettings;
  private long chainId;

  public TransactionBuilder(@NonNull TokenInfo tokenInfo) {
    contractAddress(tokenInfo.address).decimals(tokenInfo.decimals)
        .symbol(tokenInfo.symbol);
    if (!tokenInfo.symbol.equals("ETH")) {
      shouldSendToken(true);
    }
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
  }

  public long getChainId() {
    return chainId;
  }

  public TransactionBuilder setChainId(long chainId) {
    this.chainId = chainId;
    return this;
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
        + ", gasSettings="
        + gasSettings
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
  }
}
