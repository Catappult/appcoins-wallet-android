package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.BalanceUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static com.asfoundation.wallet.C.USD_SYMBOL;
import static com.asfoundation.wallet.util.BalanceUtils.weiToEth;

public class GetDefaultWalletBalance {

  private final WalletRepositoryType walletRepository;
  private final EthereumNetworkRepositoryType ethereumNetworkRepository;
  private final FetchTokensInteract fetchTokensInteract;

  public GetDefaultWalletBalance(WalletRepositoryType walletRepository,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      FetchTokensInteract fetchTokensInteract) {
    this.walletRepository = walletRepository;
    this.ethereumNetworkRepository = ethereumNetworkRepository;
    this.fetchTokensInteract = fetchTokensInteract;
  }

  public Single<Map<String, String>> get(Wallet wallet) {
    return fetchTokensInteract.fetchDefaultToken(wallet)
        .flatMapSingle(token -> {
          if (wallet.address.equals(token.tokenInfo.address)) {
            return getEtherBalance(wallet);
          } else {
            return getTokenBalance(token);
          }
        })
        .firstOrError();
  }

  private Single<Map<String, String>> getTokenBalance(Token token) {
    Map<String, String> balances = new HashMap<>();
    balances.put(token.tokenInfo.symbol, weiToEth(token.balance).setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString());
    balances.put(USD_SYMBOL, BalanceUtils.ethToUsd(token.ticker.price, token.balance.toString()));
    return Single.just(balances);
  }

  private Single<Map<String, String>> getEtherBalance(Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .flatMap(ethBalance -> {
          Map<String, String> balances = new HashMap<>();
          balances.put(ethereumNetworkRepository.getDefaultNetwork().symbol,
              weiToEth(ethBalance).setScale(4, RoundingMode.HALF_UP)
                  .stripTrailingZeros()
                  .toPlainString());
          return Single.just(balances);
        })
        .flatMap(balances -> ethereumNetworkRepository.getTicker()
            .observeOn(Schedulers.io())
            .flatMap(ticker -> {
              String ethBallance = balances.get(ethereumNetworkRepository.getDefaultNetwork().symbol);
              balances.put(USD_SYMBOL, BalanceUtils.ethToUsd(ticker.price, ethBallance));
              return Single.just(balances);
            })
            .onErrorResumeNext(throwable -> Single.just(balances)))
        .observeOn(AndroidSchedulers.mainThread());
  }
}