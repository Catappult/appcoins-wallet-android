package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class ApproveKeyProvider {
  private final Billing billing;

  public @Inject ApproveKeyProvider(Billing billing) {
    this.billing = billing;
  }

  Single<Transaction> getTransaction(String packageName, String productName,
      String transactionType) {
    BillingSupportedType billingType = BillingSupportedType.valueOfInsensitive(transactionType);
    return billing.getSkuTransaction(packageName, productName, Schedulers.io(), billingType);
  }
}
