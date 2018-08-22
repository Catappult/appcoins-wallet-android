package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.billing.BillingFactory;
import com.appcoins.wallet.billing.repository.entity.Transaction;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ApproveKeyProvider {
  private final BillingFactory billingFactory;

  public ApproveKeyProvider(BillingFactory billingFactory) {
    this.billingFactory = billingFactory;
  }

  Single<String> getKey(String packageName, String productName) {
    return billingFactory.getBilling(packageName)
        .getSkuTransaction(productName, Schedulers.io())
        .map(Transaction::getUid);
  }
}
