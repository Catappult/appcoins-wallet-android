package com.asfoundation.wallet.ui.iab;

import com.asfoundation.wallet.billing.purchase.Purchase;
import io.reactivex.Single;

public interface PurchaseProvider {
  Single<Purchase> getPurchase(String packageName, String sku);
}
