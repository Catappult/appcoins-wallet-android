package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.repository.PaymentTransaction;
import com.bds.microraidenj.MicroRaidenBDS;
import com.bds.microraidenj.channel.BDSChannel;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import java.math.BigDecimal;

public class AppcoinsRaiden implements Raiden {
  public static final String BDS_ADDRESS = "0x31a16aDF2D5FC73F149fBB779D20c036678b1bBD";
  private final PrivateKeyProvider privatekeyProvider;
  private final MicroRaidenBDS raiden;

  public AppcoinsRaiden(PrivateKeyProvider privatekeyProvider, MicroRaidenBDS raiden) {
    this.privatekeyProvider = privatekeyProvider;
    this.raiden = raiden;
  }

  @Override public Completable createChannel(String from, BigDecimal channelBudget) {
    return raiden.createChannel(privatekeyProvider.get(from), Address.from(BDS_ADDRESS),
        convertToWeis(channelBudget).toBigInteger())
        .toCompletable();
  }

  @Override public Completable buy(PaymentTransaction paymentTransaction) {
    return getChannel(paymentTransaction.getTransactionBuilder()
        .fromAddress(), bdsChannel -> bdsChannel.getReceiverAddress()
        .toString()
        .equals(BDS_ADDRESS)
        || bdsChannel.getBalance()
        .compareTo(convertToWeis(paymentTransaction.getTransactionBuilder()
            .amount()).toBigInteger()) >= 0).doOnSuccess(bdsChannel -> bdsChannel.makePayment(
        convertToWeis(paymentTransaction.getTransactionBuilder()
            .amount()).toBigInteger(), Address.from(paymentTransaction.getTransactionBuilder()
            .toAddress()), Address.from(BuildConfig.DEFAULT_STORE_ADREESS),
        Address.from(BuildConfig.DEFAULT_OEM_ADREESS)))
        .toCompletable();
  }

  @Override public Completable closeChannel(String fromAddress) {
    return getChannel(fromAddress, bdsChannel -> bdsChannel.getReceiverAddress()
        .toString()
        .equalsIgnoreCase(BDS_ADDRESS)).doOnSuccess(
        channel -> channel.closeCooperatively(privatekeyProvider.get(fromAddress)))
        .toCompletable();
  }

  private BigDecimal convertToWeis(BigDecimal amount) {
    return amount.multiply(BigDecimal.valueOf(10)
        .pow(18));
  }

  private Single<BDSChannel> getChannel(String fromAddress, Predicate<BDSChannel> filter) {
    return raiden.listChannels(privatekeyProvider.get(fromAddress), false)
        .toObservable()
        .flatMapIterable(bdsChannels -> bdsChannels)
        .filter(filter)
        .toList()
        .flatMap(bdsChannels -> {
          if (bdsChannels.isEmpty()) {
            return Single.error(new ChannelNotFoundException());
          } else {
            return Single.just(bdsChannels.get(0));
          }
        });
  }
}
