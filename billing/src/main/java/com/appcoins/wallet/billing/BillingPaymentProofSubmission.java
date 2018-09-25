package com.appcoins.wallet.billing;

import android.support.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.Single;

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