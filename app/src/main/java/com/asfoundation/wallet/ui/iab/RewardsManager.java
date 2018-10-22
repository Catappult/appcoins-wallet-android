package com.asfoundation.wallet.ui.iab;

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;
  private final Billing billing;

  public RewardsManager(AppcoinsRewards appcoinsRewards, Billing billing) {
    this.appcoinsRewards = appcoinsRewards;
    this.billing = billing;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress,
      String storeAddress, String oemAddress, String packageName, Transaction.Origin origin,
      String type) {
    return appcoinsRewards.pay(amount, origin, sku, type, developerAddress, storeAddress,
        oemAddress, packageName);
  }

  public Single<Purchase> getPaymentCompleted(String packageName, String sku) {
    return billing.getSkuPurchase(packageName, sku, Schedulers.io());
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
      case NO_NETWORK:
        return Observable.just(new RewardPayment(RewardPayment.Status.NO_NETWORK));
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
      PROCESSING, COMPLETED, ERROR, NO_NETWORK
    }
  }
}
