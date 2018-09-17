package com.asfoundation.wallet.billing;

import com.asfoundation.wallet.billing.payment.PaymentService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

public interface PaymentServiceSelector {

  Observable<PaymentService> getSelectedService(List<PaymentService> paymentServices);

  Completable selectService(PaymentService selectedPaymentService);
}
