package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(Bundle data);

  void showError();

  void close(Bundle bundle);

  void navigateToCreditCardAuthorization(boolean isBds);

  void showOnChain(BigDecimal amount, boolean isBds);

  void showCcPayment(BigDecimal amount, String currency, boolean isBds);

  void showAppcoinsCreditsPayment(BigDecimal amount);

  void showPaymentMethods(List<PaymentMethod> paymentMethods);
}
