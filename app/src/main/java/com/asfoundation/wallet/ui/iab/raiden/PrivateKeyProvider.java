package com.asfoundation.wallet.ui.iab.raiden;

import ethereumj.crypto.ECKey;
import java.math.BigInteger;

public class PrivateKeyProvider {
  public ECKey get(String walletAddress) {
    return ECKey.fromPrivate(
        new BigInteger("", 16));
  }
}
