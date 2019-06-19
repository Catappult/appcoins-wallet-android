package com.asfoundation.wallet.ui.balance.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import static com.asfoundation.wallet.ui.balance.database.BalanceDetailsEntity.TABLE_NAME;

@Entity (tableName = TABLE_NAME) public class BalanceDetailsEntity {
  @NonNull @PrimaryKey @ColumnInfo(name = "wallet_address") private final String wallet;
  @NonNull @ColumnInfo(name = "fiat_currency") private String fiatCurrency;
  @NonNull @ColumnInfo(name = "fiat_symbol") private String fiatSymbol;
  @NonNull @ColumnInfo(name = "eth_token_amount") private String ethAmount;
  @NonNull @ColumnInfo(name = "eth_token_conversion") private String ethConversion;
  @NonNull @ColumnInfo(name = "appc_token_amount") private String appcAmount;
  @NonNull @ColumnInfo(name = "appc_token_conversion") private String appcConversion;
  @NonNull @ColumnInfo(name = "credits_token_amount") private String creditsAmount;
  @NonNull @ColumnInfo(name = "credits_token_conversion") private String creditsConversion;

  static final String TABLE_NAME = "balance";

  public BalanceDetailsEntity(@NonNull String wallet) {
    this.wallet = wallet;
    this.fiatCurrency = "";
    this.fiatSymbol = "";
    this.ethAmount = "";
    this.ethConversion = "";
    this.appcAmount = "";
    this.appcConversion = "";
    this.creditsAmount = "";
    this.creditsConversion = "";
  }

  @NonNull public String getWallet() {
    return wallet;
  }

  @NonNull public String getFiatCurrency() {
    return fiatCurrency;
  }

  @NonNull public String getFiatSymbol() {
    return fiatSymbol;
  }

  @NonNull public String getEthAmount() {
    return ethAmount;
  }

  @NonNull public String getEthConversion() {
    return ethConversion;
  }

  @NonNull public String getAppcAmount() {
    return appcAmount;
  }

  @NonNull public String getAppcConversion() {
    return appcConversion;
  }

  @NonNull public String getCreditsAmount() {
    return creditsAmount;
  }

  @NonNull public String getCreditsConversion() {
    return creditsConversion;
  }

  public void setFiatCurrency(@NonNull String fiatCurrency) {
    this.fiatCurrency = fiatCurrency;
  }

  public void setFiatSymbol(@NonNull String fiatSymbol) {
    this.fiatSymbol = fiatSymbol;
  }

  public void setEthAmount(@NonNull String ethAmount) {
    this.ethAmount = ethAmount;
  }

  public void setEthConversion(@NonNull String ethConversion) {
    this.ethConversion = ethConversion;
  }

  public void setAppcAmount(@NonNull String appcAmount) {
    this.appcAmount = appcAmount;
  }

  public void setAppcConversion(@NonNull String appcConversion) {
    this.appcConversion = appcConversion;
  }

  public void setCreditsAmount(@NonNull String creditsAmount) {
    this.creditsAmount = creditsAmount;
  }

  public void setCreditsConversion(@NonNull String creditsConversion) {
    this.creditsConversion = creditsConversion;
  }

  @Override public String toString() {
    return "BalanceDetailsEntity{"
        + "wallet='"
        + wallet
        + '\''
        + ", fiatCurrency='"
        + fiatCurrency
        + '\''
        + ", fiatSymbol='"
        + fiatSymbol
        + '\''
        + ", ethAmount='"
        + ethAmount
        + '\''
        + ", ethConversion='"
        + ethConversion
        + '\''
        + ", appcAmount='"
        + appcAmount
        + '\''
        + ", appcConversion='"
        + appcConversion
        + '\''
        + ", creditsAmount='"
        + creditsAmount
        + '\''
        + ", creditsConversion='"
        + creditsConversion
        + '\''
        + '}';
  }
}
