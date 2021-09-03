package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

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

  public Operation(String transactionId, String from, String to, String fee) {
    this.transactionId = transactionId;
    this.from = from;
    this.to = to;
    this.fee = fee;
  }

  private Operation(Parcel in) {
    transactionId = in.readString();
    from = in.readString();
    to = in.readString();
    fee = in.readString();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeString(transactionId);
    parcel.writeString(from);
    parcel.writeString(to);
    parcel.writeString(fee);
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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Operation operation = (Operation) o;
    return Objects.equals(transactionId, operation.transactionId) && Objects.equals(from,
        operation.from) && Objects.equals(to, operation.to) && Objects.equals(fee, operation.fee);
  }

  @Override public int hashCode() {
    return Objects.hash(transactionId, from, to, fee);
  }
}
