package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin;
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.BillingFactory;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;
  private final BillingFactory billingFactory;

  public RewardsManager(AppcoinsRewards appcoinsRewards, BillingFactory billingFactory) {
    this.appcoinsRewards = appcoinsRewards;
    this.billingFactory = billingFactory;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress,
      String storeAddress, String oemAddress, String packageName) {
    return appcoinsRewards.pay(amount, Origin.BDS, sku, Type.INAPP, developerAddress, storeAddress,
        oemAddress, packageName);
  }

  public Single<Purchase> getPaymentCompleted(String packageName, String sku) {
    Billing billing = billingFactory.getBilling(packageName);
    return billing.getSkuPurchase(sku, Schedulers.io());
  }

  public Observable<RewardPayment> getPaymentStatus(String packageName, String sku) {
    return appcoinsRewards.getPayment(packageName, sku)
        .flatMap(this::map);
  }

  private Observable<RewardPayment> map(Transaction transaction) {
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Observable.just(new RewardPayment(RewardPayment.Status.PROCESSING));
      case COMPLETED:
        return Observable.just(new RewardPayment(RewardPayment.Status.COMPLETED));
      case ERROR:
        return Observable.just(new RewardPayment(RewardPayment.Status.ERROR));
    }
    throw new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported");
  }

  static class RewardPayment {
    private final Status status;

    RewardPayment(Status status) {
      this.status = status;
    }

    public Status getStatus() {
      return status;
    }

    enum Status {
      PROCESSING, COMPLETED, ERROR
    }
  }
}
