package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Single;

public interface PurchaseProvider {
  Single<Purchase> getPurchase(String packageName, String sku);
}
