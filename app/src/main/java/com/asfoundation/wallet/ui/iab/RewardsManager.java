package com.asfoundation.wallet.ui.iab;

import android.util.Pair;
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards;
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository;
import com.appcoins.wallet.appcoins.rewards.Transaction;
import com.appcoins.wallet.bdsbilling.Billing;
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase;
import com.asfoundation.wallet.billing.partners.AddressService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;

public class RewardsManager {
  private final AppcoinsRewards appcoinsRewards;
  private final Billing billing;
  private final AddressService partnerAddressService;

  public RewardsManager(AppcoinsRewards appcoinsRewards, Billing billing,
      AddressService partnerAddressService) {
    this.appcoinsRewards = appcoinsRewards;
    this.billing = billing;
    this.partnerAddressService = partnerAddressService;
  }

  public Completable pay(String sku, BigDecimal amount, String developerAddress, String packageName,
      String origin, String type, String payload, String callbackUrl, String orderReference) {
    return Single.zip(partnerAddressService.getStoreAddressForPackage(packageName),
        partnerAddressService.getOemAddressForPackage(packageName),
        (storeAddress, oemAddress) -> new Pair<>(storeAddress, oemAddress))
        .flatMapCompletable(
            partnersAddresses -> appcoinsRewards.pay(amount, origin, sku, type, developerAddress,
                partnersAddresses.first, partnersAddresses.second, packageName, payload,
                callbackUrl, orderReference));
  }

  public Single<Purchase> getPaymentCompleted(String packageName, String sku) {
    return billing.getSkuPurchase(packageName, sku, Schedulers.io());
  }

  public Observable<Transaction> getTransaction(String packageName, String sku, BigDecimal amount) {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString());
  }

  public Observable<RewardPayment> getPaymentStatus(String packageName, String sku,
      BigDecimal amount) {
    return appcoinsRewards.getPayment(packageName, sku, amount.toString())
        .flatMap(this::map);
  }

  private Observable<RewardPayment> map(Transaction transaction) {
    switch (transaction.getStatus()) {
      case PROCESSING:
        return Observable.just(
            new RewardPayment(transaction.getOrderReference(), RewardPayment.Status.PROCESSING));
      case COMPLETED:
        return Observable.just(
            new RewardPayment(transaction.getOrderReference(), RewardPayment.Status.COMPLETED));
      case ERROR:
        return Observable.just(
            new RewardPayment(transaction.getOrderReference(), RewardPayment.Status.ERROR));
      case NO_NETWORK:
        return Observable.just(
            new RewardPayment(transaction.getOrderReference(), RewardPayment.Status.NO_NETWORK));
    }
    throw new UnsupportedOperationException(
        "Transaction status " + transaction.getStatus() + " not supported");
  }

  public Single<AppcoinsRewardsRepository.Status> sendCredits(@NotNull String toWallet,
      BigDecimal amount, String packageName) {
    return appcoinsRewards.sendCredits(toWallet, amount, packageName);
  }

  public Single<BigDecimal> getBalance() {
    return appcoinsRewards.getBalance();
  }

  static class RewardPayment {
    private final Status status;
    private final String orderReference;

    RewardPayment(String orderReference, Status status) {
      this.orderReference = orderReference;
      this.status = status;
    }

    public String getOrderReference() {
      return orderReference;
    }

    public Status getStatus() {
      return status;
    }

    enum Status {
      PROCESSING, COMPLETED, ERROR, NO_NETWORK
    }
  }
}
