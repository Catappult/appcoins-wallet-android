package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.util.BalanceUtils.weiToEth;

public class GetDefaultWalletBalance implements BalanceService {
  private final WalletRepositoryType walletRepository;
  private final FetchTokensInteract fetchTokensInteract;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchCreditsInteract fetchCreditsInteract;
  private final NetworkInfo defaultNetwork;

  public GetDefaultWalletBalance(WalletRepositoryType walletRepository,
      FetchTokensInteract fetchTokensInteract, FindDefaultWalletInteract defaultWalletInteract,
      FetchCreditsInteract fetchCreditsInteract, NetworkInfo defaultNetwork) {
    this.walletRepository = walletRepository;
    this.fetchTokensInteract = fetchTokensInteract;
    this.defaultWalletInteract = defaultWalletInteract;
    this.fetchCreditsInteract = fetchCreditsInteract;
    this.defaultNetwork = defaultNetwork;
  }

  public Single<Balance> getTokens(Wallet wallet, int scale) {
    return fetchTokensInteract.fetchDefaultToken(wallet)
        .flatMapSingle(token -> {
          if (wallet.address.equals(token.tokenInfo.address)) {
            return getEtherBalance(wallet);
          } else {
            return getTokenBalance(token, scale);
          }
        })
        .firstOrError();
  }

  public Single<Balance> getEthereumBalance(Wallet wallet) {
    return getEtherBalance(wallet);
  }

  public Single<Balance> getCredits(Wallet wallet) {
    return fetchCreditsInteract.getBalance(wallet)
        .flatMap(credits -> getCreditsBalance(credits));
  }

  private Single<Balance> getTokenBalance(Token token, int scale) {
    return Single.just(new Balance(token.tokenInfo.symbol.toUpperCase(),
        weiToEth(token.balance).setScale(scale, RoundingMode.HALF_DOWN)
            .stripTrailingZeros()
            .toPlainString()));
  }

  private Single<Balance> getCreditsBalance(BigDecimal value) {
    return Single.just(new Balance("APPC-C", weiToEth(value).setScale(2, RoundingMode.HALF_DOWN)
        .stripTrailingZeros()
        .toPlainString()));
  }

  private Single<Balance> getEtherBalance(Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .flatMap(ethBalance -> {
          return Single.just(new Balance(defaultNetwork.symbol,
              weiToEth(ethBalance).setScale(4, RoundingMode.HALF_DOWN)
                  .stripTrailingZeros()
                  .toPlainString()));
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

  private BalanceState mapToState(Boolean enoughEther, boolean enoughTokens) {
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
      return getToken(contractAddress).map(
          token -> normalizeBalance(token.balance, token.tokenInfo).compareTo(cost) >= 0);
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

  private BigDecimal normalizeBalance(BigDecimal balance, TokenInfo tokenInfo) {
    return convertToMainMetric(balance, tokenInfo.decimals);
  }

  private BigDecimal convertToMainMetric(BigDecimal value, int decimals) {
    try {
      StringBuilder divider = new StringBuilder(18);
      divider.append("1");
      for (int i = 0; i < decimals; i++) {
        divider.append("0");
      }
      return value.divide(new BigDecimal(divider.toString()), decimals, RoundingMode.DOWN);
    } catch (NumberFormatException ex) {
      return BigDecimal.ZERO;
    }
  }

  public enum BalanceState {
    NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
  }
}