package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;

public class TransactionsMapper {
  public static final String APPROVE_METHOD_ID = "0x095ea7b3";
  public static final String BUY_METHOD_ID = "0xdc9564d5";
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
      if (isAppcoinsTransaction(transaction, address)) {
        RawTransaction auxTransaction = transactions[i - 1];
        if (isIabTransaction(auxTransaction)) {
          transactionList.add(mapIabTransaction(transaction, auxTransaction));
          i--;
          continue;
        }
        throw new IllegalStateException("unknown transaction type");
      } else {
        transactionList.add(mapStandardTransaction(transaction));
      }
    }
    return transactionList;
  }

  private Transaction mapStandardTransaction(RawTransaction transaction) {
    return new Transaction(transaction.hash);
  }

  private Transaction mapIabTransaction(RawTransaction approveTransaction,
      RawTransaction transaction) {
    return new IabTransaction(approveTransaction.hash, transaction.hash);
  }

  private boolean isIabTransaction(RawTransaction auxTransaction) {
    return auxTransaction.input.toUpperCase()
        .startsWith(BUY_METHOD_ID.toUpperCase());
  }

  private boolean isAppcoinsTransaction(RawTransaction transaction, String address) {
    return transaction.to.equalsIgnoreCase(address) && transaction.input.toUpperCase()
        .startsWith(APPROVE_METHOD_ID.toUpperCase());
  }
}
