package com.appcoins.wallet.bdsbilling;

import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.jetbrains.annotations.Nullable;

public interface BillingPaymentProofSubmission {
  Single<Transaction> processPurchaseProof(PaymentProof paymentProof);

  Single<Transaction> processAuthorizationProof(AuthorizationProof authorizationProof);

  Completable registerPaymentProof(String paymentId, String paymentProof, String paymentType);

  Single<Transaction> registerAuthorizationProof(String id, String paymentType,
      @Nullable String productName, String packageName, BigDecimal priceValue,
      String developerWallet, String storeWallet, String origin, String type, String oemWallet,
      @Nullable String developerPayload, @Nullable String callback, @Nullable String orderReference,
      @Nullable String referrerUrl);

  void saveTransactionId(Transaction transaction);

  Transaction getTransactionFromUid(String uid);

  @Nullable String getTransactionId(String buyHash);
}