package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import com.adyen.core.PaymentRequest;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import io.reactivex.Observable;

/**
 * Created by franciscocalado on 30/07/2018.
 */

public interface AdyenAuthorizationView {

  void showProduct();

  void showLoading();

  void hideLoading();

  Observable<Object> errorDismisses();

  Observable<PaymentDetails> paymentMethodDetailsEvent();

  Observable<PaymentMethod> changeCardMethodDetailsEvent();

  void showNetworkError();

  Observable<Object> cancelEvent();

  void showCvcView(Amount amount, PaymentMethod paymentMethod);

  void showCreditCardView(PaymentMethod paymentMethod, Amount amount, boolean cvcStatus,
      boolean allowSave, String publicKey, String generationTime);

  void close(Bundle bundle);

  void showWalletAddress(String address);

  void showSuccess();

  void showPaymentRefusedError(AdyenAuthorization adyenAuthorization);

  void showGenericError();
}
