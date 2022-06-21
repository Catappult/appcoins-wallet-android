package com.asfoundation.wallet.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Wallet implements Parcelable {
  public static final Creator<Wallet> CREATOR = new Creator<Wallet>() {
    @Override public Wallet createFromParcel(Parcel in) {
      return new Wallet(in);
    }

    @Override public Wallet[] newArray(int size) {
      return new Wallet[size];
    }
  };

  public String getAddress() {
    return address;
  }

  private final String address;

  public Wallet(String address) {
    this.address = address;
  }

  private Wallet(Parcel in) {
    address = in.readString();
  }

  public boolean hasSameAddress(String address) {
    return this.address.equals(address);
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(address);
  }
}
