package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.repository.PaymentTransaction;
import io.reactivex.Completable;
import java.math.BigDecimal;

public interface Raiden {
  Completable createChannel(String toAddress, BigDecimal toBigInteger);

  Completable buy(PaymentTransaction paymentTransaction);

  Completable closeChannel(String fromAddress);
}
