package com.asfoundation.wallet.repository;

import androidx.annotation.NonNull;
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission;
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService;
import com.appcoins.wallet.core.network.microservices.model.Transaction;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProvider;
import com.appcoins.wallet.core.utils.jvm_common.CountryCodeProviderKt;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
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
  private final AddressService partnerAddressService;
  private final BillingPaymentProofSubmission billingPaymentProofSubmission;

  public BuyService(WatchedTransactionService transactionService,
      TransactionValidator transactionValidator, DefaultTokenProvider defaultTokenProvider,
      CountryCodeProvider countryCodeProvider, AddressService partnerAddressService,
      BillingPaymentProofSubmission billingPaymentProofSubmission) {
    this.transactionService = transactionService;
    this.transactionValidator = transactionValidator;
    this.defaultTokenProvider = defaultTokenProvider;
    this.countryCodeProvider = countryCodeProvider;
    this.partnerAddressService = partnerAddressService;
    this.billingPaymentProofSubmission = billingPaymentProofSubmission;
  }

  public void start() {
    transactionService.start();
  }

  public Completable buy(String key, PaymentTransaction paymentTransaction) {
    TransactionBuilder transactionBuilder = paymentTransaction.getTransactionBuilder();
    Transaction cachedTransaction = billingPaymentProofSubmission.getTransactionFromUid(key);
    String storeAddress = getStoreAddress(cachedTransaction);
    String oemAddress = getOemAddress(cachedTransaction);
    return Single.zip(countryCodeProvider.getCountryCode(), defaultTokenProvider.getDefaultToken(),
        (countryCode, tokenInfo) -> transactionBuilder.appcoinsData(
            getBuyData(transactionBuilder, tokenInfo, paymentTransaction.getPackageName(),
                countryCode, storeAddress, oemAddress)))
        .map(transaction -> updateTransactionBuilderData(paymentTransaction, transaction))
        .flatMap(payment -> transactionValidator.validate(paymentTransaction)
            .map(__ -> payment))
        .flatMapCompletable(
            payment -> transactionService.sendTransaction(key, payment.getTransactionBuilder()));
  }

  private String getStoreAddress(Transaction transaction) {
    String tmpStoreAddress = null;
    if (transaction != null && transaction.getWallets() != null) {
      tmpStoreAddress = transaction.getWallets()
          .getStore();
    }
    return partnerAddressService.getStoreAddress(tmpStoreAddress);
  }

  private String getOemAddress(Transaction transaction) {
    String tmpOemAddress = null;
    if (transaction != null && transaction.getWallets() != null) {
      tmpOemAddress = transaction.getWallets()
          .getOem();
    }
    return partnerAddressService.getOemAddress(tmpOemAddress);
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
        paymentTransaction.getOrderReference(), paymentTransaction.getErrorCode(),
        paymentTransaction.getErrorMessage());
  }

  private byte[] getBuyData(TransactionBuilder transactionBuilder, TokenInfo tokenInfo,
      String packageName, String countryCode, String storeAddress, String oemAddress) {
    return TokenRepository.buyData(transactionBuilder.toAddress(), storeAddress, oemAddress,
        transactionBuilder.getSkuId(), transactionBuilder.amount()
            .multiply(new BigDecimal("10").pow(transactionBuilder.decimals())), tokenInfo.address,
        packageName, CountryCodeProviderKt.convertCountryCode(countryCode));
  }

  private BuyTransaction mapTransaction(WatchedTransaction transaction) {
    return new BuyTransaction(transaction.getKey(), transaction.getTransactionBuilder(),
        mapState(transaction.getStatus()), transaction.getTransactionHash());
  }

  private Status mapState(WatchedTransaction.Status status) {
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
      case FORBIDDEN:
        toReturn = Status.FORBIDDEN;
        break;
      case SUB_ALREADY_OWNED:
        toReturn = Status.SUB_ALREADY_OWNED;
        break;
    }
    return toReturn;
  }

  public enum Status {
    BUYING, BOUGHT, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER,
    NO_FUNDS, NO_INTERNET, PENDING, FORBIDDEN, SUB_ALREADY_OWNED
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
