package com.asfoundation.wallet.util;

import androidx.annotation.Nullable;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marat on 10/11/17.
 * Parses out protocol, address and parameters from a URL originating in QR codes used by wallets &
 * token exchanges.
 *
 * Examples:
 * - Trust wallet
 * - HITBTC
 * - MEW
 * - JAXX
 * - Plain Ethereum address
 */

public class QRUri {
  private static final int ADDRESS_LENGTH = 42;

  private final String protocol;
  private final String address;
  private final Map<String, String> parameters;

  private QRUri(String protocol, String address, Map<String, String> parameters) {
    this.protocol = protocol;
    this.address = address;
    this.parameters = Collections.unmodifiableMap(parameters);
  }

  // Be lenient and allow invalid characters in address
  private static boolean isValidAddress(String address) {
    return !TextUtils.isEmpty(address) && address.length() == ADDRESS_LENGTH;
  }

  @Nullable private static String extractAddress(String str) {
    String address = null;
    if (str.length() >= ADDRESS_LENGTH) {
      address = str.substring(0, ADDRESS_LENGTH)
          .toLowerCase();
    }
    return isValidAddress(address) ? address : null;
  }

  @Nullable public static QRUri parse(String url) {
    String[] parts = url.split(":");
    QRUri result = null;
    if (parts.length == 1) {
      String address = extractAddress(parts[0]);
      if (!TextUtils.isEmpty(address)) {
        result = new QRUri("", address.toLowerCase(), Collections.emptyMap());
      }
    } else if (parts.length == 2) {
      String protocol = parts[0];
      String address = extractAddress(parts[1]);
      if (!TextUtils.isEmpty(address)) {
        Map<String, String> params = new HashMap<>();
        String[] afterProtocol = parts[1].split("\\?");
        if (afterProtocol.length == 2) {
          String paramString = afterProtocol[1];
          List<String> paramParts = Arrays.asList(paramString.split("&"));
          params = parseParamsFromParamParts(paramParts);
        }
        result = new QRUri(protocol, address, params);
      }
    }
    return result;
  }

  private static Map<String, String> parseParamsFromParamParts(@Nullable List<String> paramParts) {
    Map<String, String> params = new HashMap<>();
    if (paramParts == null || paramParts.isEmpty()) {
      return params;
    }
    for (String pairStr : paramParts) {
      String[] pair = pairStr.split("=");
      if (pair.length < 2) {
        params.put(pair[0], "");
      } else {
        params.put(pair[0], pair[1]);
      }
    }
    return params;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getAddress() {
    return address;
  }

  public String getParameter(String key) {
    return parameters.get(key);
  }
}
