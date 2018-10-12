package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingFactory;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.asfoundation.wallet.repository.BdsPendingTransactionService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;
  private final BillingFactory billingFactory;
  private final BdsPendingTransactionService bdsPendingTransactionService;

  public RewardsManager(AppcoinsRewards appcoinsRewards, BillingFactory billingFactory,
      BdsPendingTransactionService bdsPendingTransactionService) {
    this.appcoinsRewards = appcoinsRewards;
    this.billingFactory = billingFactory;
    this.bdsPendingTransactionService = bdsPendingTransactionService;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress,
      String storeAddress, String oemAddress, String packageName) {
    return appcoinsRewards.pay(amount, Origin.BDS, sku, Type.INAPP, developerAddress, storeAddress,
        oemAddress, packageName);
  }

  public Single<Purchase> getPaymentCompleted(String packageName, String sku) {
    Billing billing = billingFactory.getBilling(packageName);
    return billing.getSkuTransaction(sku, Schedulers.io())
        .flatMap(transaction -> bdsPendingTransactionService.checkTransactionStateFromTransactionId(
            transaction.getUid())
            .ignoreElements()
            .andThen(billing.getSkuPurchase(sku, Schedulers.io())));
  }

  public Observable<Transaction> getPaymentStatus(String packageName, String sku) {
    return appcoinsRewards.getPayment(packageName, sku);
  }
}
