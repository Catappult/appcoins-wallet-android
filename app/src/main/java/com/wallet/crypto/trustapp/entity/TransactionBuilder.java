package com.wallet.crypto.trustapp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

import io.reactivex.annotations.NonNull;

public class TransactionBuilder implements Parcelable {
    private String contractAddress;
    private int decimals;
    private String symbol;
    private boolean shouldSendToken;

    private String toAddress;
    private BigDecimal amount;

    private String tokenData;

    public TransactionBuilder(@NonNull TokenInfo tokenInfo) {
        contractAddress(tokenInfo.address)
            .decimals(tokenInfo.decimals)
            .symbol(tokenInfo.symbol)
            .shouldSendToken(true);
    }

    public TransactionBuilder(@NonNull String symbol) {
        symbol(symbol);
    }

    private TransactionBuilder(Parcel in) {
        contractAddress = in.readString();
        decimals = in.readInt();
        symbol = in.readString();
        shouldSendToken = in.readInt() == 1;
        toAddress = in.readString();
        amount = new BigDecimal(in.readString());
        tokenData = in.readString();
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

    public void tokenData(String data) {
        tokenData = data;
    }

    public String tokenData() {
        return tokenData;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contractAddress);
        dest.writeInt(decimals);
        dest.writeString(symbol);
        dest.writeInt(shouldSendToken ? 1 : 0);
        dest.writeString(toAddress);
        dest.writeString(amount.toString());
        dest.writeString(tokenData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionBuilder> CREATOR = new Creator<TransactionBuilder>() {
        @Override
        public TransactionBuilder createFromParcel(Parcel in) {
            return new TransactionBuilder(in);
        }

        @Override
        public TransactionBuilder[] newArray(int size) {
            return new TransactionBuilder[size];
        }
    };
}
