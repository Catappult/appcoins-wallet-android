package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.AuthorizationProof;
import com.appcoins.wallet.billing.PaymentProof;
import io.reactivex.Observable;
import java.util.List;

public class InAppPurchaseProofSource {
  private final InAppPurchaseService inAppPurchaseService;
  private final List<String> submittedAuthorizations;
  private final List<String> submittedPayments;

  public InAppPurchaseProofSource(InAppPurchaseService inAppPurchaseService,
      List<String> submittedAuthorizations, List<String> submittedPayments) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.submittedAuthorizations = submittedAuthorizations;
    this.submittedPayments = submittedPayments;
  }

  public Observable<AuthorizationProof> getAuthorization() {
    return inAppPurchaseService.getAll()
        .flatMapIterable(paymentTransactions -> paymentTransactions)
        .filter(paymentTransaction -> paymentTransaction.getApproveHash() != null
            && !submittedAuthorizations.contains(paymentTransaction.getApproveHash()))
        .doOnNext(
            paymentTransaction -> submittedAuthorizations.add(paymentTransaction.getApproveHash()))
        .map(this::mapAuthorizationProof);
  }

  private AuthorizationProof mapAuthorizationProof(PaymentTransaction paymentTransaction) {
    return new AuthorizationProof("appcoins", paymentTransaction.getApproveHash(),
        paymentTransaction.getTransactionBuilder()
            .getSkuId(), paymentTransaction.getPackageName(),
        com.asf.wallet.BuildConfig.DEFAULT_STORE_ADDRESS,
        com.asf.wallet.BuildConfig.DEFAULT_OEM_ADDRESS, paymentTransaction.getTransactionBuilder()
        .toAddress());
  }

  public Observable<PaymentProof> getPayment() {
    return inAppPurchaseService.getAll()
        .flatMapIterable(paymentTransactions -> paymentTransactions)
        .filter(paymentTransaction -> paymentTransaction.getBuyHash() != null
            && !submittedAuthorizations.contains(paymentTransaction.getBuyHash()))
        .doOnNext(paymentTransaction -> submittedPayments.add(paymentTransaction.getBuyHash()))
        .map(this::mapPaymentProof);
  }

  private PaymentProof mapPaymentProof(PaymentTransaction paymentTransaction) {
    return new PaymentProof("appcoins", paymentTransaction.getApproveHash(),
        paymentTransaction.getBuyHash(), paymentTransaction.getTransactionBuilder()
        .getSkuId(), paymentTransaction.getPackageName(),
        com.asf.wallet.BuildConfig.DEFAULT_STORE_ADDRESS,
        com.asf.wallet.BuildConfig.DEFAULT_OEM_ADDRESS);
  }
}
