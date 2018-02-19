package com.asf.wallet.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class TokenInfo implements Parcelable {
  public static final Creator<TokenInfo> CREATOR = new Creator<TokenInfo>() {
    @Override public TokenInfo createFromParcel(Parcel in) {
      return new TokenInfo(in);
    }

    @Override public TokenInfo[] newArray(int size) {
      return new TokenInfo[size];
    }
  };
  public final String address;
  public final String name;
  public final String symbol;
  public final int decimals;
  public final boolean isEnabled;
  public final boolean isAddedManually;

  public TokenInfo(String address, String name, String symbol, int decimals, boolean isEnabled,
      boolean isAddedManually) {
    this.address = address;
    this.name = name;
    this.symbol = symbol;
    this.decimals = decimals;
    this.isEnabled = isEnabled;
    this.isAddedManually = isAddedManually;
  }

  private TokenInfo(Parcel in) {
    address = in.readString();
    name = in.readString();
    symbol = in.readString();
    decimals = in.readInt();
    isEnabled = in.readInt() == 1;
    isAddedManually = in.readInt() == 1;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(address);
    dest.writeString(name);
    dest.writeString(symbol);
    dest.writeInt(decimals);
    dest.writeInt(isEnabled ? 1 : 0);
    dest.writeInt(isAddedManually ? 1 : 0);
  }
}
