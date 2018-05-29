package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

/**
 * Created by Joao Raimundo on 18/05/2018.
 */
public class TransactionDetails  implements Parcelable {

  String sourceName;
  String icon;
  String description;

  public TransactionDetails(String sourceName, String icon, String description) {
    this.sourceName = sourceName;
    this.icon = icon;
    this.description = description;
  }

  protected TransactionDetails(Parcel in) {
    sourceName = in.readString();
    icon = in.readString();
    description = in.readString();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(sourceName);
    dest.writeString(icon);
    dest.writeString(description);
  }

  @Override public int describeContents() {
    return 0;
  }

  public static final Creator<TransactionDetails> CREATOR = new Creator<TransactionDetails>() {
    @Override public TransactionDetails createFromParcel(Parcel in) {
      return new TransactionDetails(in);
    }

    @Override public TransactionDetails[] newArray(int size) {
      return new TransactionDetails[size];
    }
  };

  public String getSourceName() {
    return sourceName;
  }

  public String getIcon() {
    return icon;
  }

  public String getDescription() {
    return description;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TransactionDetails that = (TransactionDetails) o;
    return Objects.equals(sourceName, that.sourceName)
        && Objects.equals(icon, that.icon)
        && Objects.equals(description, that.description);
  }

  @Override public int hashCode() {
    return Objects.hash(sourceName, icon, description);
  }

  @Override public String toString() {
    return "TransactionDetails{"
        + "sourceName='"
        + sourceName
        + '\''
        + ", icon='"
        + icon
        + '\''
        + ", description='"
        + description
        + '\''
        + '}';
  }
}
