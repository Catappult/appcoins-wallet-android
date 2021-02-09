package com.asfoundation.wallet.transactions;

import android.os.Parcel;
import android.os.Parcelable;
import javax.annotation.Nullable;

public class Metadata implements Parcelable {
  public static final Creator<Metadata> CREATOR = new Creator<Metadata>() {
    @Override public Metadata createFromParcel(Parcel in) {
      return new Metadata(in);
    }

    @Override public Metadata[] newArray(int size) {
      return new Metadata[size];
    }
  };

  @Nullable private final Voucher voucher;

  public Metadata(@Nullable Voucher voucher) {
    this.voucher = voucher;
  }

  protected Metadata(Parcel in) {
    voucher = in.readParcelable(Voucher.class.getClassLoader());
  }

  @Nullable public Voucher getVoucher() {
    return voucher;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(voucher, flags);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Metadata)) return false;

    Metadata metadata = (Metadata) o;

    return voucher != null ? voucher.equals(metadata.voucher) : metadata.voucher == null;
  }

  @Override public int hashCode() {
    return voucher != null ? voucher.hashCode() : 0;
  }

  @Override public int describeContents() {
    return 0;
  }
}
