package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;

public class Operation implements Parcelable {
  public static final Creator<Operation> CREATOR = new Creator<Operation>() {
    @Override public Operation createFromParcel(Parcel in) {
      return new Operation(in);
    }

    @Override public Operation[] newArray(int size) {
      return new Operation[size];
    }
  };

  private String transactionId;
  private String from;
  private String to;
  private String fee;
  private String currency;
  private String detailsUrl;

  Operation(String transactionId, String from, String to, String fee, String currency,
      String detailsUrl) {
    this.transactionId = transactionId;
    this.from = from;
    this.to = to;
    this.fee = fee;
    this.currency = currency;
    this.detailsUrl = detailsUrl;
  }

  private Operation(Parcel in) {
    transactionId = in.readString();
    from = in.readString();
    to = in.readString();
    fee = in.readString();
    currency = in.readString();
    detailsUrl = in.readString();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(transactionId);
    parcel.writeString(from);
    parcel.writeString(to);
    parcel.writeString(fee);
    parcel.writeString(currency);
    parcel.writeString(detailsUrl);
  }

  @Override public String toString() {
    return "Operation{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", from='"
        + from
        + '\''
        + ", to='"
        + to
        + '\''
        + ", fee='"
        + fee
        + '\''
        + ", currency='"
        + currency
        + '\''
        + ", detailsUrl='"
        + detailsUrl
        + '\''
        + '}';
  }

  public String getTransactionId() {
    return transactionId;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public String getFee() {
    return fee;
  }

  public String getCurrency() {
    return currency;
  }

  public String getDetailsUrl() {
    return detailsUrl;
  }
}
