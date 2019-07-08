package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.asfoundation.wallet.billing.adyen.PaymentType;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public interface IabView {

  void disableBack();

  void finish(Bundle data);

  void showError();

  void close(Bundle bundle);

  void navigateToAdyenAuthorization(boolean isBds, String currency, PaymentType paymentType,
      String bonus);

  void navigateToWebViewAuthorization(String url);

  void showOnChain(BigDecimal amount, boolean isBds, String bonus);

  void showAdyenPayment(BigDecimal amount, String currency, boolean isBds, PaymentType paymentType,
      String bonus);

  void showAppcoinsCreditsPayment(BigDecimal amount);

  void showLocalPayment(String domain, String skuId, String originalAmount, String currency,
      String bonus, String selectedPaymentMethod, boolean isInApp, String developerAddress);

  void showPaymentMethodsView();

  void showShareLinkPayment(String domain, String skuId, String originalAmount,
      String originalCurrency, BigDecimal amount, @NotNull String type,
      String selectedPaymentMethod);
}
