package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ApproveKeyProvider {
  private final Billing billing;

  public ApproveKeyProvider(Billing billing) {
    this.billing = billing;
  }

  Single<String> getKey(String packageName, String productName) {
    return billing.getSkuTransaction(packageName, productName, Schedulers.io())
        .map(Transaction::getUid);
  }
}
