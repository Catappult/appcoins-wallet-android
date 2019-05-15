package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void finish(Bundle data);

  void showError();

  void close(Bundle bundle);

  void navigateToAdyenAuthorization(boolean isBds, String currency, PaymentType paymentType);

  void navigateToWebViewAuthorization(String url);

  void showOnChain(BigDecimal amount, boolean isBds);

  void showAdyenPayment(BigDecimal amount, String currency, boolean isBds, PaymentType paymentType);

  void showAppcoinsCreditsPayment(BigDecimal amount);

  void showPaymentMethodsView();

  void showShareLinkPayment(String domain, String skuId, String originalAmount,
      String originalCurrency, BigDecimal amount, @NotNull String type);
}
