package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static com.asfoundation.wallet.util.BalanceUtils.weiToEth;

public class GetDefaultWalletBalance implements BalanceService {

  private final WalletRepositoryType walletRepository;
  private final EthereumNetworkRepositoryType ethereumNetworkRepository;
  private final FetchTokensInteract fetchTokensInteract;
  private final FindDefaultWalletInteract defaultWalletInteract;

  public GetDefaultWalletBalance(WalletRepositoryType walletRepository,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      FetchTokensInteract fetchTokensInteract, FindDefaultWalletInteract defaultWalletInteract) {
    this.walletRepository = walletRepository;
    this.ethereumNetworkRepository = ethereumNetworkRepository;
    this.fetchTokensInteract = fetchTokensInteract;
    this.defaultWalletInteract = defaultWalletInteract;
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
    Map<String, String> balance = new HashMap<>();
    balance.put(token.tokenInfo.symbol,
        weiToEth(token.balance).setScale(4, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString());
    return Single.just(balance);
  }

  private Single<Map<String, String>> getEtherBalance(Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .flatMap(ethBalance -> {
          Map<String, String> balance = new HashMap<>();
          balance.put(ethereumNetworkRepository.getDefaultNetwork().symbol,
              weiToEth(ethBalance).setScale(4, RoundingMode.HALF_UP)
                  .stripTrailingZeros()
                  .toPlainString());
          return Single.just(balance);
        })
        .observeOn(AndroidSchedulers.mainThread());
  }

  @Override public Single<BalanceState> hasEnoughBalance(TransactionBuilder transactionBuilder,
      BigDecimal transactionGasLimit) {
    GasSettings gasSettings = transactionBuilder.gasSettings();
    return Single.zip(hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit)),
        hasEnoughForTransfer(transactionBuilder.amount(), transactionBuilder.shouldSendToken(),
            gasSettings.gasPrice.multiply(transactionGasLimit),
            transactionBuilder.contractAddress()), this::mapToState);
  }

  public BalanceState mapToState(Boolean enoughEther, boolean enoughTokens) {
    if (enoughTokens && enoughEther) {
      return BalanceState.OK;
    } else if (!enoughTokens && !enoughEther) {
      return BalanceState.NO_ETHER_NO_TOKEN;
    } else if (enoughEther) {
      return BalanceState.NO_TOKEN;
    } else {
      return BalanceState.NO_ETHER;
    }
  }

  private Single<Boolean> hasEnoughForFee(BigDecimal cost) {
    return getBalanceInWei().map(ethBalance -> ethBalance.compareTo(cost) >= 0);
  }

  private Single<BigDecimal> getBalanceInWei() {
    return defaultWalletInteract.find()
        .flatMap(walletRepository::balanceInWei);
  }

  private Single<Boolean> hasEnoughForTransfer(BigDecimal cost, boolean isTokenTransfer,
      BigDecimal feeCost, String contractAddress) {
    if (isTokenTransfer) {
      return getToken(contractAddress).map(token -> token.balance.compareTo(cost) >= 0);
    }
    return getBalanceInWei().map(ethBalance -> ethBalance.subtract(feeCost)
        .compareTo(cost) >= 0);
  }

  private Single<Token> getToken(String contractAddress) {
    return defaultWalletInteract.find()
        .flatMapObservable(fetchTokensInteract::fetch)
        .firstOrError()
        .flatMap(tokens -> {
          for (Token token : tokens) {
            if (token.tokenInfo.address.equalsIgnoreCase(contractAddress)) {
              return Single.just(token);
            }
          }
          return Single.error(new UnknownTokenException());
        });
  }

  public enum BalanceState {
    NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
  }
}