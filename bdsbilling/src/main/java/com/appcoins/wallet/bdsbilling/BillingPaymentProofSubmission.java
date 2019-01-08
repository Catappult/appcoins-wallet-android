package com.appcoins.wallet.bdsbilling;

import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.jetbrains.annotations.Nullable;

public interface BillingPaymentProofSubmission {
  Completable processPurchaseProof(PaymentProof paymentProof);

  Completable processAuthorizationProof(AuthorizationProof authorizationProof);

  Completable registerPaymentProof(String paymentId, String paymentProof, String paymentType);

  Single<String> registerAuthorizationProof(String id, String paymentType,
      @Nullable String productName, String packageName, BigDecimal priceValue,
      String developerWallet, String storeWallet, String origin, String type, String oemWallet,
      @Nullable String developerPayload, @Nullable String callback,
      @Nullable String orderReference);

  void saveTransactionId(String key);

  @Nullable String getTransactionId(String buyHash);
}