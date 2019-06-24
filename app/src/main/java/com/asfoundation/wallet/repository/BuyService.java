package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.asfoundation.wallet.billing.partners.AddressService;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.CountryCodeProvider;
import com.asfoundation.wallet.poa.DataMapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class BuyService {
  private final WatchedTransactionService transactionService;
  private final TransactionValidator transactionValidator;
  private final DefaultTokenProvider defaultTokenProvider;
  private final CountryCodeProvider countryCodeProvider;
  private final DataMapper dataMapper;
  private final AddressService partnerAddressService;

  public BuyService(WatchedTransactionService transactionService,
      TransactionValidator transactionValidator, DefaultTokenProvider defaultTokenProvider,
      CountryCodeProvider countryCodeProvider, DataMapper dataMapper,
      AddressService partnerAddressService) {
    this.transactionService = transactionService;
    this.transactionValidator = transactionValidator;
    this.defaultTokenProvider = defaultTokenProvider;
    this.countryCodeProvider = countryCodeProvider;
    this.dataMapper = dataMapper;
    this.partnerAddressService = partnerAddressService;
  }

  public void start() {
    transactionService.start();
  }

  public Completable buy(String key, PaymentTransaction paymentTransaction) {
    TransactionBuilder transactionBuilder = paymentTransaction.getTransactionBuilder();
    return Single.zip(countryCodeProvider.getCountryCode(), defaultTokenProvider.getDefaultToken(),
        partnerAddressService.getStoreAddressForPackage(paymentTransaction.getPackageName()),
        partnerAddressService.getOemAddressForPackage(paymentTransaction.getPackageName()),
        (countryCode, tokenInfo, storeAddress, oemAddress) -> transactionBuilder.appcoinsData(
              getBuyData(transactionBuilder, tokenInfo, paymentTransaction.getPackageName(),
                  countryCode, storeAddress, oemAddress)))
        .map(transaction -> updateTransactionBuilderData(paymentTransaction,
        transaction)).flatMapCompletable(
        payment -> Completable.defer(() -> transactionValidator.validate(payment))
            .andThen(transactionService.sendTransaction(key, payment.getTransactionBuilder())));
  }

  public Observable<BuyTransaction> getBuy(String uri) {
    return transactionService.getTransaction(uri)
        .map(this::mapTransaction);
  }

  public Observable<List<BuyTransaction>> getAll() {
    return transactionService.getAll()
        .flatMapSingle(entries -> Observable.fromIterable(entries)
            .map(this::mapTransaction)
            .toList());
  }

  public Completable remove(String key) {
    return transactionService.remove(key);
  }

  @NonNull
  private PaymentTransaction updateTransactionBuilderData(PaymentTransaction paymentTransaction,
      TransactionBuilder transaction) {
    return new PaymentTransaction(paymentTransaction.getUri(), transaction,
        paymentTransaction.getState(), paymentTransaction.getApproveHash(),
        paymentTransaction.getBuyHash(), paymentTransaction.getPackageName(),
        paymentTransaction.getProductName(), paymentTransaction.getProductId(),
        paymentTransaction.getDeveloperPayload(), paymentTransaction.getCallbackUrl(),
        paymentTransaction.getOrderReference());
  }

  private byte[] getBuyData(TransactionBuilder transactionBuilder, TokenInfo tokenInfo,
      String packageName, String countryCode, String storeAddress, String oemAddress) {
    return TokenRepository.buyData(transactionBuilder.toAddress(), storeAddress, oemAddress,
        transactionBuilder.getSkuId(), transactionBuilder.amount()
            .multiply(new BigDecimal("10").pow(transactionBuilder.decimals())), tokenInfo.address,
        packageName, dataMapper.convertCountryCode(countryCode));
  }

  private BuyTransaction mapTransaction(Transaction transaction) {
    return new BuyTransaction(transaction.getKey(), transaction.getTransactionBuilder(),
        mapState(transaction.getStatus()), transaction.getTransactionHash());
  }

  private Status mapState(Transaction.Status status) {
    Status toReturn;
    switch (status) {
      case PENDING:
        toReturn = Status.PENDING;
        break;
      case PROCESSING:
        toReturn = Status.BUYING;
        break;
      case COMPLETED:
        toReturn = Status.BOUGHT;
        break;
      default:
      case ERROR:
        toReturn = Status.ERROR;
        break;
      case WRONG_NETWORK:
        toReturn = Status.WRONG_NETWORK;
        break;
      case NONCE_ERROR:
        toReturn = Status.NONCE_ERROR;
        break;
      case UNKNOWN_TOKEN:
        toReturn = Status.UNKNOWN_TOKEN;
        break;
      case NO_TOKENS:
        toReturn = Status.NO_TOKENS;
        break;
      case NO_ETHER:
        toReturn = Status.NO_ETHER;
        break;
      case NO_FUNDS:
        toReturn = Status.NO_FUNDS;
        break;
      case NO_INTERNET:
        toReturn = Status.NO_INTERNET;
        break;
    }
    return toReturn;
  }

  public enum Status {
    BUYING, BOUGHT, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER, NO_FUNDS, NO_INTERNET, PENDING

  }

  public static class BuyTransaction {
    private final String key;
    private final TransactionBuilder transactionBuilder;
    private final Status status;
    private final String transactionHash;

    public BuyTransaction(String key, TransactionBuilder transactionBuilder, Status status,
        String transactionHash) {
      this.key = key;
      this.transactionBuilder = transactionBuilder;
      this.status = status;
      this.transactionHash = transactionHash;
    }

    public String getKey() {
      return key;
    }

    public TransactionBuilder getTransactionBuilder() {
      return transactionBuilder;
    }

    public Status getStatus() {
      return status;
    }

    public String getTransactionHash() {
      return transactionHash;
    }
  }
}
