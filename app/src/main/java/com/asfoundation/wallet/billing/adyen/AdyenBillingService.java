package com.asfoundation.wallet.billing.adyen;

import com.adyen.core.models.Payment;
import com.appcoins.wallet.bdsbilling.WalletService;
import com.asfoundation.wallet.billing.BillingService;
import com.asfoundation.wallet.billing.TransactionService;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.jakewharton.rxrelay2.BehaviorRelay;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdyenBillingService implements BillingService {

  private final BehaviorRelay<AdyenAuthorization> relay;
  private final TransactionService transactionService;

  private final AtomicBoolean processingPayment;
  private final Adyen adyen;
  private final WalletService walletService;
  private final String merchantName;
  private volatile AdyenAuthorization adyenAuthorization;
  private volatile String transactionUid;
  private final AddressService partnerAddressService;

  public AdyenBillingService(String merchantName, TransactionService transactionService,
      WalletService walletService, Adyen adyen, AddressService partnerAddressService) {
    this.merchantName = merchantName;
    this.adyen = adyen;
    this.relay = BehaviorRelay.create();
    this.transactionService = transactionService;
    this.walletService = walletService;
    this.partnerAddressService = partnerAddressService;

    this.processingPayment = new AtomicBoolean();
  }

  @Override public Observable<AdyenAuthorization> getAuthorization(String productName,
      String developerAddress, String payload, String origin, BigDecimal priceValue,
      String priceCurrency, String type, String callback, String orderReference,
      String appPackageName) {
    return relay.doOnSubscribe(
        disposable -> startPaymentIfNeeded(productName, developerAddress, payload, origin,
            priceValue, priceCurrency, type, callback, orderReference, appPackageName))
        .doOnNext(this::resetProcessingFlag);
  }

  @Override
  public Observable<AdyenAuthorization> getAuthorization(String origin, BigDecimal priceValue,
      String priceCurrency, String type, String appPackageName) {
    return getAuthorization(null, null, null, origin, priceValue, priceCurrency, type, null, null,
        appPackageName);
  }

  @Override public Completable authorize(Payment payment, String paykey) {
    return Single.fromCallable(() -> payment.getPaymentStatus()
        .equals(Payment.PaymentStatus.AUTHORISED))
        .flatMapCompletable(authorized -> walletService.getWalletAddress()
            .flatMapCompletable(walletAddress -> walletService.signContent(walletAddress)
                .flatMapCompletable(signedContent -> {
                  if (!processingPayment.get()) {
                    return Completable.complete();
                  } else {
                    return transactionService.finishTransaction(walletAddress, signedContent,
                        transactionUid, paykey)
                        .andThen(Completable.fromAction(() -> callRelay(authorized)));
                  }
                })));
  }

  @Override public String getTransactionUid() {
    return transactionUid;
  }

  private void callRelay(boolean authorized) {
    if (authorized) {
      relay.accept(new AdyenAuthorization(adyenAuthorization.getSession(),
          AdyenAuthorization.Status.REDEEMED));
    } else {
      relay.accept(new AdyenAuthorization(adyenAuthorization.getSession(),
          AdyenAuthorization.Status.FAILED));
    }
  }

  private void resetProcessingFlag(AdyenAuthorization adyenAuthorization) {
    if (adyenAuthorization.isCompleted() || adyenAuthorization.isFailed()) {
      processingPayment.set(false);
    }
  }

  private void startPaymentIfNeeded(String productName, String developerAddress, String payload,
      String origin, BigDecimal priceValue, String priceCurrency, String type, String callback,
      String orderReference, String appPackageName) {
    if (!processingPayment.getAndSet(true)) {
      this.adyenAuthorization = walletService.getWalletAddress()
          .flatMap(walletAddress -> walletService.signContent(walletAddress)
              .flatMap(signedContent -> adyen.getToken()
                  .flatMap(token -> Single.zip(
                      partnerAddressService.getStoreAddressForPackage(appPackageName),
                      partnerAddressService.getOemAddressForPackage(appPackageName),
                      (storeAddress, oemAddress) -> transactionService.createTransaction(
                          walletAddress, signedContent, token, merchantName, payload, productName,
                          developerAddress, storeAddress, oemAddress, origin, walletAddress,
                          priceValue, priceCurrency, type, callback, orderReference))
                      .flatMap(transactionUid -> transactionUid)
                      .doOnSuccess(transactionUid -> this.transactionUid = transactionUid)
                      .flatMap(transactionUid -> transactionService.getSession(walletAddress,
                          signedContent, transactionUid)))))
          .map(this::newDefaultAdyenAuthorization)
          .blockingGet();

      relay.accept(adyenAuthorization);
    }
  }

  private AdyenAuthorization newDefaultAdyenAuthorization(String session) {
    return new AdyenAuthorization(session, AdyenAuthorization.Status.PENDING);
  }
}
