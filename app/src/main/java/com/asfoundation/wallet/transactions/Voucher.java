package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;

public class Voucher implements Parcelable {
  public static final Creator<Voucher> CREATOR = new Creator<Voucher>() {
    @Override public Voucher createFromParcel(Parcel in) {
      return new Voucher(in);
    }

    @Override public Voucher[] newArray(int size) {
      return new Voucher[size];
    }
  };

  private final String code;
  private final String redeem;

  public Voucher(String code, String redeem) {
    this.code = code;
    this.redeem = redeem;
  }

  protected Voucher(Parcel in) {
    code = in.readString();
    redeem = in.readString();
  }

  public String getCode() {
    return code;
  }

  public String getRedeem() {
    return redeem;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(code);
    dest.writeString(redeem);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Voucher)) return false;

    Voucher voucher = (Voucher) o;

    if (!code.equals(voucher.code)) return false;
    return redeem.equals(voucher.redeem);
  }

  @Override public int hashCode() {
    int result = code.hashCode();
    result = 31 * result + redeem.hashCode();
    return result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public String toString() {
    return "Voucher{" + "code='" + code + '\'' + ", redeem='" + redeem + '\'' + '}';
  }
}
