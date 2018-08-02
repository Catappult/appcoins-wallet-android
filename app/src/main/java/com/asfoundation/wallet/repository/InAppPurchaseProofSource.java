package com.asfoundation.wallet.repository;

import com.appcoins.wallet.billing.AuthorizationProof;
import io.reactivex.Observable;
import java.util.List;

public class InAppPurchaseProofSource {
  private final InAppPurchaseService inAppPurchaseService;
  private final List<String> list;

  public InAppPurchaseProofSource(InAppPurchaseService inAppPurchaseService, List<String> list) {
    this.inAppPurchaseService = inAppPurchaseService;
    this.list = list;
  }

  public Observable<AuthorizationProof> get() {
    return inAppPurchaseService.getAll()
        .flatMapIterable(paymentTransactions -> paymentTransactions)
        .filter(paymentTransaction -> paymentTransaction.getApproveHash() != null && !list.contains(
            paymentTransaction.getApproveHash()))
        .doOnNext(paymentTransaction -> list.add(paymentTransaction.getApproveHash()))
        .map(this::map);
  }

  private AuthorizationProof map(PaymentTransaction paymentTransaction) {
    return new AuthorizationProof("appcoins", paymentTransaction.getApproveHash(),
        paymentTransaction.getTransactionBuilder()
            .getSkuId(), paymentTransaction.getPackageName(),
        com.asf.wallet.BuildConfig.DEFAULT_STORE_ADDRESS,
        com.asf.wallet.BuildConfig.DEFAULT_OEM_ADDRESS);
  }
}
