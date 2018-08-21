package com.asfoundation.wallet.transactions;

import android.support.annotation.Nullable;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionOperation;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.ui.iab.AppCoinsOperation;
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver;
import com.asfoundation.wallet.util.BalanceUtils;
import com.bds.microraidenj.ws.ChannelHistoryResponse;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.asfoundation.wallet.transactions.Transaction.TransactionType.MICRO_IAB;

public class TransactionsMapper {
  public static final String APPROVE_METHOD_ID = "0x095ea7b3";
  public static final String BUY_METHOD_ID = "0xdc9564d5";
  public static final String ADS_METHOD_ID = "0x79c6b667";
  public static final String OPEN_CHANNEL_METHOD_ID = "0xa6d15963";
  public static final String TOPUP_CHANNEL_METHOD_ID = "0x016a8cf6";
  public static final String CLOSE_CHANNEL_METHOD_ID = "0x1c6f609b";
  private final DefaultTokenProvider defaultTokenProvider;
  private final AppcoinsOperationsDataSaver operationsDataSaver;
  private final Scheduler scheduler;

  public TransactionsMapper(DefaultTokenProvider defaultTokenProvider,
      AppcoinsOperationsDataSaver operationsDataSaver, Scheduler scheduler) {
    this.defaultTokenProvider = defaultTokenProvider;
    this.operationsDataSaver = operationsDataSaver;
    this.scheduler = scheduler;
  }

  public Single<List<Transaction>> map(RawTransaction[] transactions) {
    return defaultTokenProvider.getDefaultToken()
        .observeOn(scheduler)
        .map(tokenInfo -> map(tokenInfo.address, transactions));
  }

  public Single<List<Transaction>> map(List<ChannelHistoryResponse.MicroTransaction> transactions) {
    return Single.just(mapMicroTransactions(transactions)).observeOn(scheduler);
  }

  private List<Transaction> map(String address, RawTransaction[] transactions) {
    List<Transaction> transactionList = new ArrayList<>();
    for (int i = transactions.length - 1; i >= 0; i--) {
      RawTransaction transaction = transactions[i];
      if (isAppcoinsTransaction(transaction, address)
          && isApprovedTransaction(transaction)
          && i > 0
          && isTransactionWithApprove(transactions[i - 1])) {
        transactionList.add(0, mapTransactionWithApprove(transaction, transactions[i - 1]));
        i--;
      } else if (isAdsTransaction(transaction)) {
        transactionList.add(0, mapAdsTransaction(transaction));
      } else if (isCloseChannleTransaction(transaction)) {
        transactionList.add(0, mapCloseChannelTransaction(transaction));
      } else {
        transactionList.add(0, mapStandardTransaction(transaction));
      }
    }
    return transactionList;
  }

  private List<Transaction> mapMicroTransactions(List<ChannelHistoryResponse.MicroTransaction> transactions) {
    List<Transaction> transactionList = new ArrayList<>();
    for (int i = transactions.size() - 1; i >= 0; i--) {
      ChannelHistoryResponse.MicroTransaction transaction = transactions.get(i);

      transactionList.add(0, new Transaction(transaction.getTxID(), MICRO_IAB, null,
          transaction.getTs()
              .getTime() / 1000, Transaction.TransactionStatus.SUCCESS, transaction.getAmount()
          .toString(), transaction.getSender(), transaction.getReceiver(),
          getTransactionDetails(MICRO_IAB, transaction.getTxID()), "APPC", null));
    }
    return transactionList;
  }

  /**
   * Method to map a raw transaction to an Ads transaction. In this case the raw transaction value
   * does not contain the value of the transfer, that information is in the operations contained in
   * the raw transaction.
   * NOTE: For the value of this transaction we are considering the value of the first operation,
   * by relying on the order that the ads transactions are done. Only the first operation includes
   * the value that will be earned by the user.
   *
   * @param transaction The raw transaction including all the information for a given transaction.
   *
   * @return a Transaction object containing the information needed and formatted, ready to be shown
   * on the transactions list.
   */
  private Transaction mapAdsTransaction(RawTransaction transaction) {
    String value = transaction.value;
    String currency = null;
    String from = transaction.from;
    String to = transaction.to;
    List<Operation> operations = new ArrayList<>();
    String fee = BalanceUtils.weiToEth(
        new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)))
        .toPlainString();

    if (transaction.operations != null && transaction.operations.length > 0) {
      TransactionOperation operation = transaction.operations[0];
      value = operation.value;
      currency = operation.contract.symbol;
      from = operation.from;
      to = operation.to;

      operations.add(new Operation(transaction.hash, operation.from, operation.to, fee));
    } else {

      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee));
    }

    TransactionDetails details =
        getTransactionDetails(Transaction.TransactionType.ADS, transaction.hash);

    return new Transaction(transaction.hash, Transaction.TransactionType.ADS, null,
        transaction.timeStamp, getError(transaction), value, from, to, details, currency,
        operations);
  }

  /**
   * Method to map a raw transaction to a close channel transaction. In this case most probably the
   * raw transaction value contains the value of the transfer, but to make sure that is the case, we
   * confirm that there is no operation inside the raw transaction. In case the operations list is
   * not empty we make the assumption that the value on the first operation of the list is the one
   * to be taken in consideration for the user.
   *
   * @param transaction The raw transaction including all the information for a given transaction.
   *
   * @return a Transaction object containing the information needed and formatted, ready to be shown
   * on the transactions list.
   */
  private Transaction mapCloseChannelTransaction(RawTransaction transaction) {
    String value = transaction.value;
    String currency = null;
    String from = transaction.from;
    String to = transaction.to;

    List<Operation> operations = new ArrayList<>();
    String fee = BalanceUtils.weiToEth(
        new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)))
        .toPlainString();

    if (transaction.operations != null && transaction.operations.length > 0) {
      for (TransactionOperation operation : transaction.operations) {
        operations.add(new Operation(transaction.hash, operation.from, operation.to, fee));
        if (operation.to.equals(transaction.from)) {
          currency = operation.contract.symbol;
          from = operation.from;
          to = operation.to;
          value = operation.value;
        }
      }
    } else {
      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee));
    }

    return new Transaction(transaction.hash, Transaction.TransactionType.CLOSE_CHANNEL, null,
        transaction.timeStamp, getError(transaction), value, from, to, null, currency, operations);
  }

  /**
   * Method to map a raw transaction to a standard transaction. In this case most probably the raw
   * transaction value contains the value of the transfer, but to make sure that is the case, we
   * confirm that there is no operation inside the raw transaction. In case the operations list is
   * not empty we make the assumption that the value on the first operation of the list is the one
   * to be taken in consideration for the user.
   *
   * @param transaction The raw transaction including all the information for a given transaction.
   *
   * @return a Transaction object containing the information needed and formatted, ready to be shown
   * on the transactions list.
   */
  private Transaction mapStandardTransaction(RawTransaction transaction) {
    String value = transaction.value;
    String currency = null;
    List<Operation> operations = new ArrayList<>();
    String fee = BalanceUtils.weiToEth(
        new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)))
        .toPlainString();

    if (transaction.operations != null && transaction.operations.length > 0) {
      TransactionOperation operation = transaction.operations[0];
      value = operation.value;
      currency = operation.contract.symbol;

      operations.add(new Operation(transaction.hash, operation.from, operation.to, fee));
    } else {

      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee));
    }

    return new Transaction(transaction.hash, Transaction.TransactionType.STANDARD, null,
        transaction.timeStamp, getError(transaction), value, transaction.from, transaction.to, null,
        currency, operations);
  }

  /**
   * Method to map a raw transaction to an IAB transaction. In this case all the transfer mentioned
   * in the operations list on the raw transaction need to be summed to obtained the value of the
   * transaction, since the user transfer the value that afterwards is split between all the parties
   * included in the iab transaction.
   *
   * @param approveTransaction The raw transaction for the approve transaction.
   * @param transaction The raw transaction including all the information for a given transaction.
   *
   * @return a Transaction object containing the information needed and formatted, ready to be shown
   * on the transactions list.
   */
  private Transaction mapTransactionWithApprove(RawTransaction approveTransaction,
      RawTransaction transaction) {
    BigInteger value = new BigInteger(transaction.value);
    String currency = null;
    List<Operation> operations = new ArrayList<>();

    String fee = BalanceUtils.weiToEth(new BigDecimal(approveTransaction.gasUsed).multiply(
        new BigDecimal(approveTransaction.gasPrice)))
        .toPlainString();
    if (approveTransaction.operations != null && approveTransaction.operations.length > 0) {
      currency = approveTransaction.operations[0].contract.symbol;

      operations.add(
          new Operation(approveTransaction.hash, approveTransaction.from, approveTransaction.to,
              fee));
    } else {
      operations.add(
          new Operation(approveTransaction.hash, approveTransaction.from, approveTransaction.to,
              fee));
    }

    fee = BalanceUtils.weiToEth(
        new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)))
        .toPlainString();
    if (transaction.operations != null && transaction.operations.length > 0) {
      currency = transaction.operations[0].contract.symbol;
      for (TransactionOperation operation : transaction.operations) {
        value = value.add(new BigInteger(operation.value));
      }

      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee));
    }

    Transaction.TransactionType type = getTransactionType(transaction);
    TransactionDetails details = getTransactionDetails(type, transaction.hash);

    return new Transaction(transaction.hash, type, approveTransaction.hash, transaction.timeStamp,
        getError(transaction), value.toString(), transaction.from, transaction.to, details,
        currency, operations);
  }

  private boolean isAdsTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(ADS_METHOD_ID.toUpperCase());
  }

  private boolean isIabTransaction(RawTransaction auxTransaction) {
    return auxTransaction.input.toUpperCase()
        .startsWith(BUY_METHOD_ID.toUpperCase());
  }

  private boolean isApprovedTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(APPROVE_METHOD_ID.toUpperCase());
  }

  private boolean isOpenChannelTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(OPEN_CHANNEL_METHOD_ID.toUpperCase());
  }

  private boolean isTopUpChannelTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(TOPUP_CHANNEL_METHOD_ID.toUpperCase());
  }

  private boolean isCloseChannleTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(CLOSE_CHANNEL_METHOD_ID.toUpperCase());
  }

  private boolean isAppcoinsTransaction(RawTransaction transaction, String address) {
    return transaction.to.equalsIgnoreCase(address);
  }

  private boolean isTransactionWithApprove(RawTransaction auxTransaction) {
    return auxTransaction.input.toUpperCase()
        .startsWith(BUY_METHOD_ID.toUpperCase()) || auxTransaction.input.toUpperCase()
        .startsWith(OPEN_CHANNEL_METHOD_ID.toUpperCase()) || auxTransaction.input.toUpperCase()
        .startsWith(TOPUP_CHANNEL_METHOD_ID.toUpperCase());
  }

  private Transaction.TransactionStatus getError(RawTransaction transaction) {
    return (transaction.error == null || transaction.error.isEmpty())
        ? Transaction.TransactionStatus.SUCCESS : Transaction.TransactionStatus.FAILED;
  }

  @Nullable private TransactionDetails getTransactionDetails(Transaction.TransactionType type,
      String transactionId) {
    TransactionDetails details = null;
    AppCoinsOperation operationDetails = operationsDataSaver.getSync(transactionId);
    if (operationDetails != null) {
      String productName = null;
      if (!Transaction.TransactionType.ADS.equals(type)) {
        productName = operationDetails.getProductName();
      }
      details = new TransactionDetails(operationDetails.getApplicationName(),
          operationDetails.getIconPath(), productName);
    }
    return details;
  }

  private Transaction.TransactionType getTransactionType(RawTransaction transaction) {
    Transaction.TransactionType type = Transaction.TransactionType.STANDARD;
    if (isIabTransaction(transaction)) {
      type = Transaction.TransactionType.IAB;
    } else if (isOpenChannelTransaction(transaction)) {
      type = Transaction.TransactionType.OPEN_CHANNEL;
    } else if (isTopUpChannelTransaction(transaction)) {
      type = Transaction.TransactionType.TOP_UP_CHANNEL;
    }

    return type;
  }
}
