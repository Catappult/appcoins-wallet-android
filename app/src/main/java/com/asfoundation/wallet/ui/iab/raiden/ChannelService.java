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
  private final Repository<String, ChannelPayment> cache;

  public ChannelService(Raiden raiden, Repository<String, ChannelPayment> cache) {
    this.raiden = raiden;
    this.cache = cache;
  }

  public void start() {
    cache.getAll()
        .flatMapIterable(channelPayments -> channelPayments)
        .filter(channelPayment -> channelPayment.getStatus()
            .equals(ChannelPayment.Status.PENDING))
        .flatMapSingle(payment -> raiden.buy(payment.getFromAddress(), payment.getAmmount(),
            payment.getToAddress())
            .doOnSubscribe(disposable -> updatePaymentStatus(payment, ChannelPayment.Status.BUYING))
            .doOnSuccess(hash -> cache.saveSync(payment.getId(),
                new ChannelPayment(payment.getId(), ChannelPayment.Status.COMPLETED,
                    payment.getFromAddress(), payment.getAmmount(), payment.getToAddress(),
                    payment.getChannelBudget(), hash)))
            .retryWhen(throwableFlowable -> throwableFlowable.flatMap(
                throwable -> createChannelOrError(payment, throwable)))
            .doOnError(throwable -> updatePaymentStatus(payment, ChannelPayment.Status.ERROR)))
        .doOnError(throwable -> throwable.printStackTrace())
        .retry()
        .subscribe();
  }

  private Publisher<?> createChannelOrError(ChannelPayment payment, Throwable throwable) {
    if (throwable instanceof ChannelNotFoundException) {
      return createChannel(payment).andThen(Observable.just(new Object()))
          .toFlowable(BackpressureStrategy.DROP);
    } else {
      return Flowable.error(throwable);
    }
  }

  private Completable createChannel(ChannelPayment payment) {
    return raiden.createChannel(payment.getFromAddress(), payment.getChannelBudget());
  }

  private void updatePaymentStatus(ChannelPayment payment, ChannelPayment.Status status) {
    cache.saveSync(payment.getId(), new ChannelPayment(status, payment));
  }

  public void buy(PaymentTransaction paymentTransaction, BigDecimal channelBudget) {
    cache.saveSync(paymentTransaction.getUri(),
        new ChannelPayment(paymentTransaction.getUri(), ChannelPayment.Status.PENDING,
            paymentTransaction.getTransactionBuilder()
                .fromAddress(), paymentTransaction.getTransactionBuilder()
            .amount(), paymentTransaction.getTransactionBuilder()
            .toAddress(), channelBudget));
  }

  public Observable<ChannelPayment> getPayment(String key) {
    return cache.get(key)
        .filter(channelPayment -> !channelPayment.getStatus()
            .equals(ChannelPayment.Status.PENDING));
  }

  public Completable remove(String key) {
    return cache.remove(key);
  }

  public Single<Boolean> hasChannel(String wallet) {
    return raiden.hasChannel(wallet);
  }
}
