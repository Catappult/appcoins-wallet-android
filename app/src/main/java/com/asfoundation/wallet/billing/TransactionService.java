package com.asfoundation.wallet.billing;

import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface TransactionService {

  Single<String> createTransaction(String address, String signature, String token,
      String packageName, String payload, String productName, String developerWallet,
      String storeWallet, String oemWallet, String origin, String walletAddress,
      BigDecimal priceValue, String priceCurrency, String type, String callback,
      String orderReference);

  Single<String> getSession(String address, String signature, String transactionUid);

  Completable finishTransaction(String address, String signature, String transactionUid, String paykey);
}
