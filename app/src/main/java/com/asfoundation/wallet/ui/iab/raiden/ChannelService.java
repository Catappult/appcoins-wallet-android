package com.asfoundation.wallet.ui.iab.raiden;

import com.asfoundation.wallet.repository.PaymentTransaction;
import com.asfoundation.wallet.repository.Repository;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import org.reactivestreams.Publisher;

public class ChannelService {
  private final Raiden raiden;
  private final Repository<String, ChannelPayment> paymentCache;
  private final Repository<String, ChannelCreation> channelCache;

  public ChannelService(Raiden raiden, Repository<String, ChannelPayment> paymentCache,
      Repository<String, ChannelCreation> channelCache) {
    this.raiden = raiden;
    this.paymentCache = paymentCache;
    this.channelCache = channelCache;
  }

  public void start() {
    paymentCache.getAll()
        .flatMapIterable(channelPayments -> channelPayments)
        .filter(channelPayment -> channelPayment.getStatus()
            .equals(ChannelPayment.Status.PENDING))
        .flatMapSingle(payment -> raiden.buy(payment.getFromAddress(), payment.getAmmount(),
            payment.getToAddress())
            .doOnSubscribe(disposable -> updatePaymentStatus(payment, ChannelPayment.Status.BUYING))
            .doOnSuccess(hash -> paymentCache.saveSync(payment.getId(),
                new ChannelPayment(payment.getId(), ChannelPayment.Status.COMPLETED,
                    payment.getFromAddress(), payment.getAmmount(), payment.getToAddress(),
                    payment.getChannelBudget(), hash)))
            .retryWhen(throwableFlowable -> throwableFlowable.flatMap(
                throwable -> createChannelOrError(payment, throwable)))
            .doOnError(throwable -> updatePaymentStatus(payment, ChannelPayment.Status.ERROR)))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe();

    channelCache.getAll()
        .flatMapIterable(channelCreations -> channelCreations)
        .filter(channelCreation -> channelCreation.getStatus()
            .equals(ChannelCreation.Status.PENDING))
        .doOnNext(channelCreation -> updateChannelStatus(channelCreation,
            ChannelCreation.Status.CREATING))
        .flatMapCompletable(channelCreation -> raiden.createChannel(channelCreation.getAddress(),
            channelCreation.getBudget())
            .andThen(channelCache.save(channelCreation.getKey(),
                new ChannelCreation(channelCreation, ChannelCreation.Status.CREATED)))
            .doOnError(
                throwable -> updateChannelStatus(channelCreation, ChannelCreation.Status.ERROR)))
        .doOnError(Throwable::printStackTrace)
        .retry()
        .subscribe();
  }

  private void updateChannelStatus(ChannelCreation channelCreation, ChannelCreation.Status status) {
    channelCache.saveSync(channelCreation.getKey(), new ChannelCreation(channelCreation, status));
  }

  private Publisher<?> createChannelOrError(ChannelPayment payment, Throwable throwable) {
    if (throwable instanceof ChannelNotFoundException) {
      return createChannel(payment.getId(), payment.getFromAddress(),
          payment.getChannelBudget()).andThen(Observable.just(new Object()))
          .toFlowable(BackpressureStrategy.DROP);
    } else {
      return Flowable.error(throwable);
    }
  }

  public Completable createChannel(String uri, String fromAddress, BigDecimal channelBudget) {
    return channelCache.save(uri,
        new ChannelCreation(uri, ChannelCreation.Status.PENDING, fromAddress, channelBudget));
  }

  private void updatePaymentStatus(ChannelPayment payment, ChannelPayment.Status status) {
    paymentCache.saveSync(payment.getId(), new ChannelPayment(status, payment));
  }

  public void buy(PaymentTransaction paymentTransaction, BigDecimal channelBudget) {
    paymentCache.saveSync(paymentTransaction.getUri(),
        new ChannelPayment(paymentTransaction.getUri(), ChannelPayment.Status.PENDING,
            paymentTransaction.getTransactionBuilder()
                .fromAddress(), paymentTransaction.getTransactionBuilder()
            .amount(), paymentTransaction.getTransactionBuilder()
            .toAddress(), channelBudget));
  }

  public Observable<ChannelPayment> getPayment(String key) {
    return paymentCache.get(key)
        .filter(channelPayment -> !channelPayment.getStatus()
            .equals(ChannelPayment.Status.PENDING));
  }

  public Observable<ChannelCreation> getChannel(String key) {
    return channelCache.get(key)
        .filter(channel -> !channel.getStatus()
            .equals(ChannelPayment.Status.PENDING));
  }

  public Completable remove(String key) {
    return paymentCache.remove(key)
        .andThen(channelCache.remove(key));
  }

  public Single<Boolean> hasChannel(String wallet) {
    return raiden.hasChannel(wallet);
  }

  public Single<Boolean> hasFunds(String wallet, BigDecimal amount) {
    return raiden.hasFunds(wallet, amount);
  }
}
