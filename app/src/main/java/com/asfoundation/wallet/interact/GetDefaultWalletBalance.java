package com.asfoundation.wallet.interact;

import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.util.BalanceUtils.weiToEth;

public class GetDefaultWalletBalance implements BalanceService {
  private final WalletRepositoryType walletRepository;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchCreditsInteract fetchCreditsInteract;
  private final NetworkInfo defaultNetwork;
  private final TokenRepositoryType tokenRepositoryType;

  public GetDefaultWalletBalance(WalletRepositoryType walletRepository,
      FindDefaultWalletInteract defaultWalletInteract, FetchCreditsInteract fetchCreditsInteract,
      NetworkInfo defaultNetwork, TokenRepositoryType tokenRepositoryType) {
    this.walletRepository = walletRepository;
    this.defaultWalletInteract = defaultWalletInteract;
    this.fetchCreditsInteract = fetchCreditsInteract;
    this.defaultNetwork = defaultNetwork;
    this.tokenRepositoryType = tokenRepositoryType;
  }

  public Single<Balance> getAppcBalance(Wallet wallet) {
    return tokenRepositoryType.getAppcBalance(wallet)
        .flatMap(this::getAppcBalance);
  }

  private Single<Token> getAppcToken(Wallet wallet) {
    return tokenRepositoryType.getAppcBalance(wallet);
  }

  public Single<Balance> getEthereumBalance(Wallet wallet) {
    return getEtherBalance(wallet);
  }

  public Single<Balance> getCredits(Wallet wallet) {
    return fetchCreditsInteract.getBalance(wallet)
        .flatMap(this::getCreditsBalance);
  }

  private Single<Balance> getAppcBalance(Token token) {
    return Single.just(new Balance(token.tokenInfo.symbol.toUpperCase(),
        weiToEth(token.balance).setScale(4, RoundingMode.FLOOR)));
  }

  private Single<Balance> getCreditsBalance(BigDecimal value) {
    return Single.just(new Balance("APPC-C", weiToEth(value).setScale(4, RoundingMode.FLOOR)));
  }

  private Single<Balance> getEtherBalance(Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .flatMap(ethBalance -> Single.just(new Balance(defaultNetwork.symbol,
            weiToEth(ethBalance).setScale(4, RoundingMode.FLOOR))))
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
        .flatMap(this::getAppcToken)
        .flatMap(token -> {
          if (token.tokenInfo.address.equalsIgnoreCase(contractAddress)) {
            return Single.just(token);
          } else {
            return Single.error(new UnknownTokenException());
          }
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