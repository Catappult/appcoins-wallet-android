package com.asfoundation.wallet.wallets;

import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FetchCreditsInteract;
import com.asfoundation.wallet.repository.BalanceService;
import com.asfoundation.wallet.repository.TokenRepositoryType;
import com.asfoundation.wallet.repository.WalletRepositoryType;
import com.asfoundation.wallet.util.UnknownTokenException;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.util.BalanceUtils.weiToEth;

public class GetDefaultWalletBalanceInteract implements BalanceService {
  private final WalletRepositoryType walletRepository;
  private final FindDefaultWalletInteract defaultWalletInteract;
  private final FetchCreditsInteract fetchCreditsInteract;
  private final NetworkInfo defaultNetwork;
  private final TokenRepositoryType tokenRepositoryType;

  public GetDefaultWalletBalanceInteract(WalletRepositoryType walletRepository,
      FindDefaultWalletInteract defaultWalletInteract, FetchCreditsInteract fetchCreditsInteract,
      NetworkInfo defaultNetwork, TokenRepositoryType tokenRepositoryType) {
    this.walletRepository = walletRepository;
    this.defaultWalletInteract = defaultWalletInteract;
    this.fetchCreditsInteract = fetchCreditsInteract;
    this.defaultNetwork = defaultNetwork;
    this.tokenRepositoryType = tokenRepositoryType;
  }

  public Single<Balance> getAppcBalance(String address) {
    return tokenRepositoryType.getAppcBalance(address)
        .flatMap(this::getAppcBalance);
  }

  private Single<Token> getAppcToken(String address) {
    return tokenRepositoryType.getAppcBalance(address);
  }

  public Single<Balance> getEthereumBalance(String address) {
    return getEtherBalance(address);
  }

  public Single<Balance> getCredits(String address) {
    return fetchCreditsInteract.getBalance(address)
        .flatMap(this::getCreditsBalance);
  }

  private Single<Balance> getAppcBalance(Token token) {
    return Single.just(new Balance(token.tokenInfo.symbol.toUpperCase(),
        weiToEth(token.balance).setScale(4, RoundingMode.FLOOR)));
  }

  private Single<Balance> getCreditsBalance(BigDecimal value) {
    return Single.just(new Balance("APPC-C", weiToEth(value).setScale(4, RoundingMode.FLOOR)));
  }

  private Single<Balance> getEtherBalance(String address) {
    return walletRepository.getEthBalanceInWei(address)
        .flatMap(ethBalance -> Single.just(new Balance(defaultNetwork.symbol,
            weiToEth(ethBalance).setScale(4, RoundingMode.FLOOR))));
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
        .flatMap((Wallet wallet) -> walletRepository.getEthBalanceInWei(wallet.address));
  }

  private Single<Boolean> hasEnoughForTransfer(BigDecimal cost, boolean isTokenTransfer,
      BigDecimal feeCost, String contractAddress) {
    if (isTokenTransfer) {
      return getAppcToken().flatMap(token -> {
        if (token.tokenInfo.address.equalsIgnoreCase(contractAddress)) {
          return Single.just(token);
        } else {
          return Single.error(new UnknownTokenException());
        }
      })
          .map(token -> normalizeBalance(token.balance, token.tokenInfo).compareTo(cost) >= 0);
    }
    return getBalanceInWei().map(ethBalance -> ethBalance.subtract(feeCost)
        .compareTo(cost) >= 0);
  }

  private Single<Token> getAppcToken() {
    return defaultWalletInteract.find()
        .flatMap(wallet -> getAppcToken(wallet.address));
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