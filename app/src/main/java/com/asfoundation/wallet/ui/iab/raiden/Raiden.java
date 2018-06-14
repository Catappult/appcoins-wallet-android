package com.asfoundation.wallet.ui.iab.raiden;

import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface Raiden {
  Completable createChannel(String toAddress, BigDecimal toBigInteger);

  Single<String> buy(String fromAddress, BigDecimal amount, String toAddress);

  Completable closeChannel(String fromAddress);

  Single<Boolean> hasChannel(String wallet);

  Single<Boolean> hasFunds(String wallet, BigDecimal amount);
}
