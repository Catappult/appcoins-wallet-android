package com.asfoundation.wallet.transactions;

import android.text.TextUtils;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionOperation;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import io.reactivex.Single;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.web3j.utils.Convert;

public class TransactionsMapper {
  public static final String APPROVE_METHOD_ID = "0x095ea7b3";
  public static final String BUY_METHOD_ID = "0xdc9564d5";
  public static final String ADS_METHOD_ID = "0x79c6b667";
  private final DefaultTokenProvider defaultTokenProvider;

  public TransactionsMapper(DefaultTokenProvider defaultTokenProvider) {
    this.defaultTokenProvider = defaultTokenProvider;
  }

  public Single<List<Transaction>> map(RawTransaction[] transactions) {
    return defaultTokenProvider.getDefaultToken()
        .map(tokenInfo -> map(tokenInfo.address, transactions));
  }

  private List<Transaction> map(String address, RawTransaction[] transactions) {
    List<Transaction> transactionList = new ArrayList<>();
    for (int i = transactions.length - 1; i >= 0; i--) {
      RawTransaction transaction = transactions[i];
      if (isAppcoinsTransaction(transaction, address)
          && isApprovedTransaction(transaction)
          && isIabTransaction(transactions[i - 1])) {
        transactionList.add(0, mapIabTransaction(transaction, transactions[i - 1]));
        i--;
      } else if (isAdsTransaction(transaction)) {
        transactionList.add(0, mapAdsTransaction(transaction));
      } else {
        transactionList.add(0, mapStandardTransaction(transaction));
      }
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
   * */
  private Transaction mapAdsTransaction(RawTransaction transaction) {
    String value = transaction.value;
    String currency = null;
    String from = transaction.from;
    String to = transaction.to;
    List<Operation> operations = new ArrayList<>();
    String fee = new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)).toString();

    if (transaction.operations != null && transaction.operations.length > 0) {
      TransactionOperation operation = transaction.operations[0];
      value = operation.value;
      currency = operation.contract.symbol;
      from = operation.from;
      to = operation.to;

      operations.add(new Operation(operation.transactionId, operation.from, operation.to, fee,
          operation.contract.symbol, ""));
    } else {

      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee,
          currency, ""));
    }

    return new Transaction(transaction.hash, Transaction.TransactionType.ADS,
        null, transaction.timeStamp, getError(transaction),
        value, from, to, null, currency, operations);
  }

  private boolean isAdsTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(ADS_METHOD_ID.toUpperCase());
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
   * */
  private Transaction mapStandardTransaction(RawTransaction transaction) {
    String value = transaction.value;
    String currency = null;
    List<Operation> operations = new ArrayList<>();
    String fee = new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice)).toString();

    if (transaction.operations != null && transaction.operations.length > 0) {
      TransactionOperation operation = transaction.operations[0];
      value = operation.value;
      currency = operation.contract.symbol;

      operations.add(new Operation(operation.transactionId, operation.from, operation.to, fee,
          currency, ""));
    } else {

      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee,
          currency, ""));
    }

    return new Transaction(transaction.hash, Transaction.TransactionType.STANDARD,
        null, transaction.timeStamp, getError(transaction),
        value, transaction.from, transaction.to, null, currency, operations);
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
   * */
  private Transaction mapIabTransaction(RawTransaction approveTransaction,
      RawTransaction transaction) {
    BigInteger value = new BigInteger(transaction.value);
    String currency = null;
    List<Operation> operations = new ArrayList<>();

    String fee = new BigDecimal(approveTransaction.gasUsed).multiply(
        new BigDecimal(approveTransaction.gasPrice))
        .toString();
    if (approveTransaction.operations != null && approveTransaction.operations.length > 0) {
      currency = approveTransaction.operations[0].contract.symbol;

      operations.add(
          new Operation(approveTransaction.hash, approveTransaction.from, approveTransaction.to,
              fee, currency, ""));
    } else {
      operations.add(new Operation(transaction.hash, transaction.from, transaction.to, fee,
          currency, ""));
    }

    fee = new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice))
            .toString();
    if (transaction.operations != null && transaction.operations.length > 0) {
      currency = transaction.operations[0].contract.symbol;
      for (TransactionOperation operation : transaction.operations) {
        value = value.add(new BigInteger(operation.value));
      }

      operations.add(
          new Operation(transaction.hash, transaction.from, transaction.to, fee, currency, ""));
    }

    return new Transaction(transaction.hash, Transaction.TransactionType.IAB,
        approveTransaction.hash, transaction.timeStamp, getError(transaction), value.toString(),
        transaction.from, transaction.to, null, currency, operations);
  }

  private boolean isIabTransaction(RawTransaction auxTransaction) {
    return auxTransaction.input.toUpperCase()
        .startsWith(BUY_METHOD_ID.toUpperCase());
  }

  private boolean isApprovedTransaction(RawTransaction transaction) {
    return transaction.input.toUpperCase()
        .startsWith(APPROVE_METHOD_ID.toUpperCase());
  }

  private boolean isAppcoinsTransaction(RawTransaction transaction, String address) {
    return transaction.to.equalsIgnoreCase(address);
  }

  private Transaction.TransactionStatus getError(RawTransaction transaction) {
    return (transaction.error == null || transaction.error.isEmpty()) ?
        Transaction.TransactionStatus.SUCCESS : Transaction.TransactionStatus.FAILED;
  }
}
