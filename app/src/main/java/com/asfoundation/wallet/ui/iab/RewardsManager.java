package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type;
import com.asfoundation.wallet.billing.purchase.Purchase;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;
  private final PurchaseProvider purchaseProvider;

  public RewardsManager(AppcoinsRewards appcoinsRewards, PurchaseProvider purchaseProvider) {
    this.appcoinsRewards = appcoinsRewards;
    this.purchaseProvider = purchaseProvider;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress,
      String storeAddress, String oemAddress, String packageName) {
    return appcoinsRewards.pay(amount, Origin.BDS, sku, Type.INAPP, developerAddress, storeAddress,
        oemAddress, packageName);
  }

  public Single<Purchase> getPaymentCompleted(String packageName, String sku) {
    return purchaseProvider.getPurchase(packageName, sku);
  }
}
