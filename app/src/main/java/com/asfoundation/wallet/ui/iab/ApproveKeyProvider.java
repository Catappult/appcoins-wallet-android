package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ApproveKeyProvider {
  private final Billing billing;

  public ApproveKeyProvider(Billing billing) {
    this.billing = billing;
  }

  Single<Transaction> getTransaction(String packageName, String productName,
      String transactionType) {
    BillingSupportedType billingType = BillingSupportedType.valueOfInsensitive(type);
    return billing.getSkuTransaction(packageName, productName, Schedulers.io(), billingType);
  }
}
