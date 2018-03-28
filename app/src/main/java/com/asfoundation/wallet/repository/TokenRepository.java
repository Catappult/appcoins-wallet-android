package com.asfoundation.wallet.repository;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import com.asf.wallet.BuildConfig;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TokenTicker;
import com.asfoundation.wallet.entity.Transaction;
import com.asfoundation.wallet.entity.TransactionOperation;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.service.TickerService;
import com.asfoundation.wallet.service.TokenExplorerClientType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import okhttp3.OkHttpClient;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;

public class TokenRepository implements TokenRepositoryType {

  private static final long BALANCE_UPDATE_INTERVAL = DateUtils.MINUTE_IN_MILLIS;
  private final TokenExplorerClientType tokenNetworkService;
  private final WalletRepositoryType walletRepository;
  private final TokenLocalSource localSource;
  private final OkHttpClient httpClient;
  private final EthereumNetworkRepositoryType ethereumNetworkRepository;
  private final TransactionLocalSource transactionsLocalCache;
  private final TickerService tickerService;
  private Web3j web3j;

  public TokenRepository(OkHttpClient okHttpClient,
      EthereumNetworkRepositoryType ethereumNetworkRepository,
      WalletRepositoryType walletRepository, TokenExplorerClientType tokenNetworkService,
      TokenLocalSource localSource, TransactionLocalSource transactionsLocalCache,
      TickerService tickerService) {
    this.httpClient = okHttpClient;
    this.ethereumNetworkRepository = ethereumNetworkRepository;
    this.walletRepository = walletRepository;
    this.tokenNetworkService = tokenNetworkService;
    this.localSource = localSource;
    this.transactionsLocalCache = transactionsLocalCache;
    this.tickerService = tickerService;
    this.ethereumNetworkRepository.addOnChangeDefaultNetwork(this::buildWeb3jClient);
    buildWeb3jClient(ethereumNetworkRepository.getDefaultNetwork());
  }

  private static Function balanceOf(String owner) {
    return new Function("balanceOf", Collections.singletonList(new Address(owner)),
        Collections.singletonList(new TypeReference<Uint256>() {
        }));
  }

  public static byte[] createTokenTransferData(String to, BigDecimal tokenAmount) {
    List<Type> params = Arrays.asList(new Address(to), new Uint256(tokenAmount.toBigInteger()));
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("transfer", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  public static byte[] createTokenApproveData(String spender, BigDecimal amount) {
    List<Type> params = Arrays.asList(new Address(spender), new Uint256(amount.toBigInteger()));
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("approve", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  public static byte[] buyData(String developerAddress, String storeAddress, String oemAddress,
      String data, BigDecimal amount, String tokenAddress) {
    Uint256 amountParam = new Uint256(amount.toBigInteger());
    Utf8String dataParam = new Utf8String(data);
    Address contractAddress = new Address(tokenAddress);
    Address developerAddressParam = new Address(developerAddress);
    Address storeAddressParam = new Address(storeAddress);
    Address oemAddressParam = new Address(oemAddress);
    List<Type> params =
        Arrays.asList(amountParam, dataParam, contractAddress, developerAddressParam,
            storeAddressParam, oemAddressParam);
    List<TypeReference<?>> returnTypes = Collections.singletonList(new TypeReference<Bool>() {
    });
    Function function = new Function("buy", params, returnTypes);
    String encodedFunction = FunctionEncoder.encode(function);
    return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
  }

  private void buildWeb3jClient(NetworkInfo defaultNetwork) {
    web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
  }

  @Override public Observable<Token[]> fetchActive(String walletAddress) {
    NetworkInfo network = ethereumNetworkRepository.getDefaultNetwork();
    Wallet wallet = new Wallet(walletAddress);
    return Single.merge(fetchCachedEnabledTokens(network, wallet), // Immediately show the cache.
        updateTokens(network, wallet) // Looking for new tokens
            .andThen(fetchCachedEnabledTokens(network, wallet))) // and showing the cach
        .map(this::removeDuplicates)
        .toObservable();
  }

  @Override public Observable<Token[]> fetchAll(String walletAddress) {
    NetworkInfo network = ethereumNetworkRepository.getDefaultNetwork();
    Wallet wallet = new Wallet(walletAddress);
    return localSource.fetchAllTokens(network, wallet)
        .toObservable();
  }

  @Override public Completable addToken(Wallet wallet, String address, String symbol, int decimals,
      boolean isAddedManually) {
    return localSource.saveTokens(ethereumNetworkRepository.getDefaultNetwork(), wallet,
        new Token[] {
            new Token(
                new TokenInfo(address, "", symbol.toLowerCase(), decimals, true, isAddedManually),
                null, 0)
        });
  }

  @Override public Completable setEnable(Wallet wallet, Token token, boolean isEnabled) {
    NetworkInfo network = ethereumNetworkRepository.getDefaultNetwork();
    return Completable.fromAction(() -> localSource.setEnable(network, wallet, token, isEnabled));
  }

  @Override public Completable delete(Wallet wallet, Token token) {
    NetworkInfo network = ethereumNetworkRepository.getDefaultNetwork();
    return localSource.delete(network, wallet, token);
  }

  private Token[] removeDuplicates(Token[] tokens) {
    List<Token> toKeep = new LinkedList<>(Arrays.asList(tokens));

    Iterator<Token> iterator = toKeep.iterator();

    while (iterator.hasNext()) {
      Token token = iterator.next();
      for (Token tmp : toKeep) {
        if (tmp != token && tmp.tokenInfo.address.toLowerCase()
            .equals(token.tokenInfo.address.toLowerCase())) {
          if (tmp.tokenInfo.name.equals("")) {
            toKeep.remove(tmp);
          } else {
            iterator.remove();
          }
          break;
        }
      }
    }

    return mapToArray(toKeep);
  }

  private Token[] mapToArray(List<Token> toKeep) {
    Token[] tokens = new Token[toKeep.size()];

    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = toKeep.get(i);
    }

    return tokens;
  }

  private SingleTransformer<Token[], Token[]> attachTicker(NetworkInfo network, Wallet wallet) {
    return upstream -> upstream.flatMap(
        tokens -> Single.zip(Single.just(tokens), getTickers(network, wallet, tokens),
            (data, tokenTickers) -> {
              for (Token token : data) {
                for (TokenTicker ticker : tokenTickers) {
                  if (token.tokenInfo.address.equalsIgnoreCase(ticker.contract)) {
                    token.ticker = ticker;
                  }
                }
              }
              return data;
            }));
  }

  private Single<TokenTicker[]> getTickers(NetworkInfo network, Wallet wallet, Token[] tokens) {
    return localSource.fetchTickers(network, wallet, tokens)
        .onErrorResumeNext(throwable -> tickerService.fetchTockenTickers(tokens, "USD")
            .onErrorResumeNext(thr -> Single.just(new TokenTicker[0])))
        .flatMapCompletable(tokenTickers -> localSource.saveTickers(network, wallet, tokenTickers))
        .andThen(localSource.fetchTickers(network, wallet, tokens)
            .onErrorResumeNext(thr -> Single.just(new TokenTicker[0])));
  }

  private Single<Token[]> fetchFromNetworkSource(@NonNull NetworkInfo network,
      @NonNull Wallet wallet) {
    return Single.fromCallable(() -> {
      try {
        return network.isMainNetwork ? tokenNetworkService.fetch(wallet.address)
            .blockingFirst() : new TokenInfo[0];
      } catch (Throwable th) {
        // Ignore all errors, it's not important source.
        return new TokenInfo[0];
      }
    })
        .map(this::mapToTokens);
  }

  private Single<Token[]> extractFromTransactions(NetworkInfo network, Wallet wallet) {
    return transactionsLocalCache.fetchTransaction(network, wallet)
        .flatMap(transactions -> {
          List<Token> result = new ArrayList<>();
          for (Transaction transaction : transactions) {
            if (transaction.operations == null || transaction.operations.length == 0) {
              continue;
            }
            TransactionOperation operation = transaction.operations[0];
            result.add(new Token(new TokenInfo(operation.contract.address, operation.contract.name,
                operation.contract.symbol, operation.contract.decimals, true, false), null, 0));
          }
          return Single.just(result.toArray(new Token[result.size()]));
        });
  }

  private Completable updateTokens(NetworkInfo network, Wallet wallet) {
    return Single.zip(fetchFromNetworkSource(network, wallet),
        extractFromTransactions(network, wallet), localSource.fetchAllTokens(network, wallet),
        (fromNetTokens, fromTrxTokens, cachedTokens) -> {
          final Set<String> oldTokensIndex = new HashSet<>();
          final List<Token> zip = new ArrayList<>();
          zip.addAll(Arrays.asList(fromNetTokens));
          zip.addAll(Arrays.asList(fromTrxTokens));
          final List<Token> newTokens = new ArrayList<>();
          for (Token cachedToken : cachedTokens) {
            oldTokensIndex.add(cachedToken.tokenInfo.address);
          }
          for (int i = zip.size() - 1; i > -1; i--) {
            if (!oldTokensIndex.contains(zip.get(i).tokenInfo.address)) {
              newTokens.add(zip.get(i));
            }
          }
          return newTokens.toArray(new Token[newTokens.size()]);
        })
        .flatMapCompletable(tokens -> localSource.saveTokens(network, wallet, tokens));
  }

  private ObservableTransformer<Token, Token> updateBalance(NetworkInfo network, Wallet wallet) {
    return upstream -> upstream.map(token -> {
      long now = System.currentTimeMillis();
      long minUpdateBalanceTime = now - BALANCE_UPDATE_INTERVAL;
      if (token.balance == null || token.updateBlancaTime < minUpdateBalanceTime) {
        try {
          token = new Token(token.tokenInfo, getBalance(wallet, token.tokenInfo), now);
          localSource.updateTokenBalance(network, wallet, token);
        } catch (Throwable th) { /* Quietly */ }
      }
      return token;
    });
  }

  private SingleTransformer<Token[], Token[]> attachEthereum(NetworkInfo network, Wallet wallet) {
    return upstream -> Single.zip(upstream, attachEth(network, wallet), (tokens, ethToken) -> {
      List<Token> result = new ArrayList<>();
      result.add(ethToken);
      result.addAll(Arrays.asList(tokens));
      return result.toArray(new Token[result.size()]);
    });
  }

  private Single<Token[]> fetchCachedEnabledTokens(NetworkInfo network, Wallet wallet) {
    return localSource.fetchEnabledTokens(network, wallet)
        .flatMapObservable(Observable::fromArray)
        .compose(updateBalance(network, wallet))
        .toList()
        .map(list -> list.toArray(new Token[list.size()]))
        .compose(attachTicker(network, wallet))
        .compose(attachEthereum(network, wallet));
  }

  private Single<Token> attachEth(NetworkInfo network, Wallet wallet) {
    return walletRepository.balanceInWei(wallet)
        .map(balance -> {
          TokenInfo info =
              new TokenInfo(wallet.address, network.name, network.symbol, 18, true, false);
          return new Token(info, balance, System.currentTimeMillis());
        })
        .flatMap(token -> ethereumNetworkRepository.getTicker()
            .map(ticker -> {
              token.ticker = new TokenTicker("", "", ticker.price, ticker.percentChange24h, null);
              return token;
            })
            .onErrorResumeNext(throwable -> Single.just(token)));
  }

  private BigDecimal getBalance(Wallet wallet, TokenInfo tokenInfo) throws Exception {
    Function function = balanceOf(wallet.address);
    String responseValue = callSmartContractFunction(function, tokenInfo.address, wallet.address);

    List<Type> response =
        FunctionReturnDecoder.decode(responseValue, function.getOutputParameters());
    if (response.size() == 1) {
      return new BigDecimal(((Uint256) response.get(0)).getValue());
    } else {
      return null;
    }
  }

  private String callSmartContractFunction(Function function, String contractAddress,
      String walletAddress) throws Exception {
    String encodedFunction = FunctionEncoder.encode(function);
    org.web3j.protocol.core.methods.request.Transaction transaction =
        createEthCallTransaction(walletAddress, contractAddress, encodedFunction);
    EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
        .send();

    return response.getValue();
  }

  private Token[] mapToTokens(TokenInfo[] items) {
    int len = items.length;
    Token[] tokens = new Token[len];
    for (int i = 0; i < len; i++) {
      tokens[i] = new Token(items[i], null, 0);
    }
    return tokens;
  }
}
