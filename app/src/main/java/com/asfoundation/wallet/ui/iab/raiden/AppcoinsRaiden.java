package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import com.asf.wallet.BuildConfig;
import com.bds.microraidenj.MicroRaidenBDS;
import com.bds.microraidenj.channel.BDSChannel;
import com.bds.microraidenj.ws.ChannelHistoryResponse;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class AppcoinsRaiden implements Raiden {
  public static final String BDS_ADDRESS = "0x31a16aDF2D5FC73F149fBB779D20c036678b1bBD";
  private final PrivateKeyProvider privatekeyProvider;
  private final MicroRaidenBDS raiden;

  public AppcoinsRaiden(PrivateKeyProvider privatekeyProvider, MicroRaidenBDS raiden) {
    this.privatekeyProvider = privatekeyProvider;
    this.raiden = raiden;
  }

  @Override public Completable createChannel(String from, BigDecimal channelBudget) {
    return privatekeyProvider.get(from)
        .flatMapCompletable(ecKey -> raiden.createChannel(ecKey, Address.from(BDS_ADDRESS),
            convertToWeis(channelBudget).toBigInteger())
            .toCompletable());
  }

  @Override public Single<String> buy(String fromAddress, BigDecimal amount, String address) {
    return getChannel(fromAddress, bdsChannel -> bdsChannel.getReceiverAddress()
        .toString()
        .equals(BDS_ADDRESS)
        || bdsChannel.getBalance()
        .compareTo(convertToWeis(amount).toBigInteger()) >= 0).flatMap(
        bdsChannel -> bdsChannel.makePayment(convertToWeis(amount).toBigInteger(),
            Address.from(address), Address.from(BuildConfig.DEFAULT_STORE_ADDRESS),
            Address.from(BuildConfig.DEFAULT_OEM_ADDRESS)));
  }

  @Override public Completable closeChannel(String fromAddress) {
    return privatekeyProvider.get(fromAddress)
        .flatMapCompletable(ecKey -> getChannel(fromAddress,
            bdsChannel -> bdsChannel.getReceiverAddress()
                .toString()
                .equalsIgnoreCase(BDS_ADDRESS)).doOnSuccess(
            channel -> channel.closeCooperatively(ecKey))
            .toCompletable());
  }

  @Override public Single<List<ChannelHistoryResponse.MicroTransaction>> fetchTransactions(
      String walletAddress) {
    return raiden.listTransactions(Address.from(walletAddress))
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<Boolean> hasChannel(String wallet) {
    return privatekeyProvider.get(wallet)
        .flatMap(ecKey -> raiden.listChannels(ecKey, false))
        .map(this::hasChannel);
  }

  private boolean hasChannel(List<BDSChannel> bdsChannels) {
    for (BDSChannel bdsChannel : bdsChannels) {
      if (bdsChannel.getReceiverAddress()
          .toString()
          .equalsIgnoreCase(BDS_ADDRESS)) {
        return true;
      }
    }
    return false;
  }

  @Override public Single<Boolean> hasFunds(String wallet, BigDecimal amount) {
    return privatekeyProvider.get(wallet)
        .flatMap(ecKey -> raiden.listChannels(ecKey, false))
        .map(bdsChannels -> hasFunds(bdsChannels, amount));
  }

  private boolean hasFunds(List<BDSChannel> bdsChannels, BigDecimal amount) {
    BigInteger amountAsBigInteger = convertToWeis(amount).toBigInteger();
    for (BDSChannel bdsChannel : bdsChannels) {
      if (bdsChannel.getBalance()
          .compareTo(amountAsBigInteger) >= 0) {
        return true;
      }
    }
    return false;
  }

  private BigDecimal convertToWeis(BigDecimal amount) {
    return amount.multiply(BigDecimal.valueOf(10)
        .pow(18));
  }

  private Single<BDSChannel> getChannel(String fromAddress, Predicate<BDSChannel> filter) {
    return privatekeyProvider.get(fromAddress)
        .flatMap(ecKey -> raiden.listChannels(ecKey, false)
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
            }));
  }
}
