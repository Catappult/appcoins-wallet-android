package com.asfoundation.wallet.ui.iab;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import rx.Observable;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public interface CreditCardAuthorizationView {

  void showProduct(double amount);

  void showLoading();

  void hideLoading();

  Observable<Void> errorDismisses();

  Observable<PaymentDetails> creditCardDetailsEvent();

  void showNetworkError();

  Observable<Void> cancelEvent();

  void showCvcView(Amount amount, PaymentMethod paymentMethod);

  void showCreditCardView(PaymentMethod paymentMethod, Amount amount, boolean cvcStatus,
      boolean allowSave, String publicKey, String generationTime);

  void close();

  void showWalletAddress(String address);

  void showSuccess();
}
