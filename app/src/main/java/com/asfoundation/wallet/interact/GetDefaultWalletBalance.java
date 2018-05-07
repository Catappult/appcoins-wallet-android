package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.BalanceUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

  public Single<String> get(Wallet wallet) {
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

  private Single<String> getTokenBalance(Token token) {
    StringBuilder balance = new StringBuilder();
    balance.append(weiToEth(token.balance).setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString())
        .append(" ")
        .append(token.tokenInfo.symbol);
    return Single.just(balance.toString());
  }

  private Single<String> getEtherBalance(Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .flatMap(ethBalance -> {
          StringBuilder balance = new StringBuilder();
          balance.append(weiToEth(ethBalance).setScale(4, RoundingMode.HALF_UP)
              .stripTrailingZeros()
              .toPlainString())
              .append(" ")
              .append(ethereumNetworkRepository.getDefaultNetwork().symbol);
          return Single.just(balance.toString());
        })
        .observeOn(AndroidSchedulers.mainThread());
  }
}