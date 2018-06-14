package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.type.Address;
import com.asf.wallet.BuildConfig;
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
            Address.from(address), Address.from(BuildConfig.DEFAULT_STORE_ADREESS),
            Address.from(BuildConfig.DEFAULT_OEM_ADREESS)));
  }

  @Override public Completable closeChannel(String fromAddress) {
    return privatekeyProvider.get(fromAddress)
        .flatMapCompletable(ecKey -> getChannel(fromAddress,
            bdsChannel -> bdsChannel.getReceiverAddress()
                .toString()
                .equalsIgnoreCase(BDS_ADDRESS)).doOnSuccess(channel -> channel.closeCooperatively(ecKey))
            .toCompletable());
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
