package com.asfoundation.wallet.billing.view.card;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.asfoundation.wallet.presenter.View;
import rx.Observable;

public interface CreditCardAuthorizationView extends View {

  void showLoading();

  void hideLoading();

  Observable<Void> errorDismisses();

  Observable<PaymentDetails> creditCardDetailsEvent();

  void showNetworkError();

  Observable<Void> cancelEvent();

  void showCvcView(Amount amount, PaymentMethod paymentMethod);

  void showCreditCardView(PaymentMethod paymentMethod, Amount amount, boolean cvcStatus,
      boolean allowSave, String publicKey, String generationTime);

  void showSuccess();
}