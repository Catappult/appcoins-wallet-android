package com.wallet.crypto.trustapp.repository;

import com.wallet.crypto.trustapp.entity.GasSettings;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.http.HttpService;

import static com.wallet.crypto.trustapp.C.DEFAULT_GAS_LIMIT;
import static com.wallet.crypto.trustapp.C.DEFAULT_GAS_LIMIT_FOR_TOKENS;
import static com.wallet.crypto.trustapp.C.DEFAULT_GAS_PRICE;

public class GasSettingsRepository implements GasSettingsRepositoryType {

  private final static long FETCH_GAS_PRICE_INTERVAL = 60;
  private final EthereumNetworkRepositoryType networkRepository;
  private BigDecimal cachedGasPrice;

  public GasSettingsRepository(EthereumNetworkRepositoryType networkRepository) {
    this.networkRepository = networkRepository;

    cachedGasPrice = new BigDecimal(DEFAULT_GAS_PRICE);
    Observable.interval(0, FETCH_GAS_PRICE_INTERVAL, TimeUnit.SECONDS)
        .doOnNext(l -> updateGasSettings())
        .subscribe(l -> {
        }, t -> {
        });
  }

  private void updateGasSettings() {
    final Web3j web3j =
        Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));
    try {
      EthGasPrice price = web3j.ethGasPrice()
          .send();
      cachedGasPrice = new BigDecimal(price.getGasPrice());
    } catch (Exception ex) { /* Quietly */ }
  }

  public Single<GasSettings> getGasSettings(boolean forTokenTransfer) {
    return Single.fromCallable(() -> {
      BigDecimal gasLimit = forTokenTransfer ? new BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS)
          : new BigDecimal(DEFAULT_GAS_LIMIT);
      if (cachedGasPrice == null) {
        updateGasSettings();
      }
      return new GasSettings(cachedGasPrice, gasLimit);
    });
  }
}
