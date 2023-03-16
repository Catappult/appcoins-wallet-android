package com.appcoins.wallet.core.utils.common;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.utils.Convert;

public class BalanceUtils {

  public static BigDecimal weiToEth(BigDecimal wei) {
    return Convert.fromWei(wei, Convert.Unit.ETHER);
  }

  public static BigDecimal weiToGweiBI(BigInteger wei) {
    return Convert.fromWei(new BigDecimal(wei), Convert.Unit.GWEI);
  }

  public static BigDecimal weiToGwei(BigDecimal wei) {
    return Convert.fromWei(wei, Convert.Unit.GWEI);
  }

  public static BigDecimal gweiToWei(BigDecimal gwei) {
    return Convert.toWei(gwei, Convert.Unit.GWEI);
  }

  /**
   * Base - taken to mean default unit for a currency e.g. ETH, DOLLARS
   * Subunit - taken to mean subdivision of base e.g. WEI, CENTS
   *
   * @param baseAmount - decimal amount in base unit of a given currency
   * @param decimals - decimal places used to convert to subunits
   *
   * @return amount in subunits
   */
  public static BigDecimal baseToSubunit(BigDecimal baseAmount, int decimals) {
    if ((decimals < 0)) throw new AssertionError();
    return baseAmount.multiply(BigDecimal.valueOf(10)
        .pow(decimals));
  }

  static public SpannableString formatBalance(String value, String currency, int currencySize,
      int currencyColor) {
    String balance = value + " " + currency.toUpperCase();
    SpannableString styledTitle = new SpannableString(balance);
    if (!TextUtils.isEmpty(currency)) {
      int currencyIndex = balance.indexOf(currency.toUpperCase());

      styledTitle.setSpan(new AbsoluteSizeSpan(currencySize), currencyIndex, balance.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      styledTitle.setSpan(new ForegroundColorSpan(currencyColor), currencyIndex, balance.length(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    return styledTitle;
  }
}
