package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.asfoundation.wallet.entity.GasSettings;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;

import static com.asfoundation.wallet.C.DEFAULT_GAS_LIMIT;
import static com.asfoundation.wallet.C.DEFAULT_GAS_LIMIT_FOR_TOKENS;
import static com.asfoundation.wallet.C.DEFAULT_GAS_PRICE;

public class GasSettingsRepository implements GasSettingsRepositoryType {

  private final static long FETCH_GAS_PRICE_INTERVAL = 60;
  private final Web3jProvider web3jProvider;
  private BigDecimal cachedGasPrice;

  public GasSettingsRepository(Web3jProvider web3jProvider) {
    this.web3jProvider = web3jProvider;

    cachedGasPrice = new BigDecimal(DEFAULT_GAS_PRICE);
    Observable.interval(0, FETCH_GAS_PRICE_INTERVAL, TimeUnit.SECONDS)
        .doOnNext(l -> updateGasSettings())
        .subscribe(l -> {
        }, t -> {
        });
  }

  private void updateGasSettings() {
    final Web3j web3j = web3jProvider.get();
    try {
      EthGasPrice price = web3j.ethGasPrice()
          .send();
      cachedGasPrice = new BigDecimal(price.getGasPrice());
    } catch (Exception ex) { /* Quietly */ }
  }

  public Single<GasSettings> getGasSettings(boolean forTokenTransfer) {
    return Single.fromCallable(() -> {
      BigDecimal gasLimit = getGasLimit(forTokenTransfer);
      if (cachedGasPrice == null) {
        updateGasSettings();
      }
      return new GasSettings(cachedGasPrice, gasLimit);
    });
  }

  @NonNull private BigDecimal getGasLimit(boolean forTokenTransfer) {
    return forTokenTransfer ? new BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS)
        : new BigDecimal(DEFAULT_GAS_LIMIT);
  }
}
