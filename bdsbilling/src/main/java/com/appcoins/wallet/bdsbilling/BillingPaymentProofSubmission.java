package com.appcoins.wallet.bdsbilling;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.jetbrains.annotations.Nullable;

public interface BillingPaymentProofSubmission {
  Completable processPurchaseProof(PaymentProof paymentProof);

  Completable processAuthorizationProof(AuthorizationProof authorizationProof);

  Completable registerPaymentProof(String paymentId, String paymentProof, String paymentType);

  Single<String> registerAuthorizationProof(String id, String paymentType, String productName,
      String packageName, String developerWallet, String storeWallet,
      @Nullable String developerPayload);

  void saveTransactionId(String key);

  @Nullable String getTransactionId(String buyHash);
}