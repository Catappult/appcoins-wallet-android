package com.asfoundation.wallet.billing;

import com.adyen.core.models.Payment;
import com.appcoins.wallet.billing.WalletService;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.billing.authorization.AdyenAuthorization;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.jakewharton.rxrelay2.BehaviorRelay;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdyenBilling implements CreditCardBilling {

  private final BehaviorRelay<AdyenAuthorization> relay;
  private final TransactionService transactionService;

  private final AtomicBoolean processingPayment;
  private final Adyen adyen;
  private final WalletService walletService;
  private final String merchantName;
  private AdyenAuthorization adyenAuthorization;
  private String transactionUid;

  public AdyenBilling(String merchantName, TransactionService transactionService,
      WalletService walletService, Adyen adyen) {
    this.merchantName = merchantName;
    this.adyen = adyen;
    this.relay = BehaviorRelay.create();
    this.transactionService = transactionService;
    this.walletService = walletService;

    this.processingPayment = new AtomicBoolean();
  }

  @Override public Observable<AdyenAuthorization> getAuthorization(String productName,
      String developerAddress, String payload, String origin, BigDecimal priceValue,
      String priceCurrency, String type) {
    return relay.doOnSubscribe(
        disposable -> startPaymentIfNeeded(productName, developerAddress, payload, origin,
            priceValue, priceCurrency, type))
        .doOnNext(this::resetProcessingFlag);
  }

  @Override public Completable authorize(Payment payment, String paykey) {
    return Single.fromCallable(() -> payment.getPaymentStatus()
        .equals(Payment.PaymentStatus.AUTHORISED))
        .flatMapCompletable(authorized -> walletService.getWalletAddress()
            .flatMapCompletable(walletAddress -> walletService.signContent(walletAddress)
                    .flatMapCompletable(
                        signedContent -> {
                          if (transactionUid == null) {
                            return Completable.complete();
                          } else {
                            return transactionService.finishTransaction(walletAddress,
                                signedContent, transactionUid, paykey)
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
      transactionUid = null;
    }
  }

  private void startPaymentIfNeeded(String productName, String developerAddress, String payload,
      String origin, BigDecimal priceValue, String priceCurrency, String type) {
    if (!processingPayment.getAndSet(true)) {
      this.adyenAuthorization = null;
      this.adyenAuthorization = walletService.getWalletAddress()
          .flatMap(walletAddress -> walletService.signContent(walletAddress)
                  .flatMap(signedContent -> adyen.createToken()
                      .flatMap(token -> transactionService.createTransaction(walletAddress,
                          signedContent, token, merchantName, payload, productName,
                          developerAddress, BuildConfig.DEFAULT_STORE_ADDRESS,
                          BuildConfig.DEFAULT_OEM_ADDRESS, origin, walletAddress, priceValue,
                          priceCurrency, type))
                      .doOnSuccess(transactionUid -> this.transactionUid = transactionUid)
                      .flatMap(__ -> transactionService.getSession(walletAddress, signedContent,
                          transactionUid))))
          .map(this::newDefaultAdyenAuthorization)
          .blockingGet();

      relay.accept(adyenAuthorization);
    }
  }

  private AdyenAuthorization newDefaultAdyenAuthorization(String session) {
    return new AdyenAuthorization(session, AdyenAuthorization.Status.PENDING);
  }
}
