package com.wallet.crypto.trustapp.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class GasSettings implements Parcelable {
    public final BigDecimal gasPrice;
    public final BigDecimal gasLimit;

    public GasSettings(BigDecimal gasPrice, BigDecimal gasLimit) {
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }

    private GasSettings(Parcel in) {
        gasPrice = new BigDecimal(in.readString());
        gasLimit = new BigDecimal(in.readString());
    }

    public static final Creator<GasSettings> CREATOR = new Creator<GasSettings>() {
        @Override
        public GasSettings createFromParcel(Parcel in) {
            return new GasSettings(in);
        }

        @Override
        public GasSettings[] newArray(int size) {
            return new GasSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(gasPrice.toString());
        parcel.writeString(gasLimit.toString());
    }
}
