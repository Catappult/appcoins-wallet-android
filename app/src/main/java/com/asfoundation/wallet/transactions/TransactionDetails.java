package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;

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
}
