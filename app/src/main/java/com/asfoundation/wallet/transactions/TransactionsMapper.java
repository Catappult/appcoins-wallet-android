package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.WalletHistory;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import static com.asfoundation.wallet.transactions.Transaction.TransactionType.ADS_OFFCHAIN;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.BONUS;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.ETHER_TRANSFER;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.IAP;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.IAP_OFFCHAIN;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.STANDARD;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.TOP_UP;
import static com.asfoundation.wallet.transactions.Transaction.TransactionType.TRANSFER_OFF_CHAIN;

public class TransactionsMapper {

  public TransactionsMapper() {
  }

  public List<Transaction> mapTransactionsFromWalletHistory(
      List<WalletHistory.Transaction> transactions) {
    List<Transaction> transactionList = new ArrayList<>(transactions.size());
    for (int i = transactions.size() - 1; i >= 0; i--) {
      WalletHistory.Transaction transaction = transactions.get(i);

      Transaction.TransactionType txType = mapTransactionType(transaction);

      Transaction.TransactionStatus status;
      switch (transaction.getStatus()) {
        case SUCCESS:
          status = Transaction.TransactionStatus.SUCCESS;
          break;
        default:
        case FAIL:
          status = Transaction.TransactionStatus.FAILED;
          break;
      }

      String sourceName;
      if (txType.equals(BONUS)) {
        if (transaction.getBonus() == null) {
          sourceName = null;
        } else {
          sourceName = transaction.getBonus()
              .stripTrailingZeros()
              .toPlainString();
        }
      } else {
        sourceName = transaction.getApp();
      }
      transactionList.add(0, new Transaction(transaction.getTxID(), txType, null,
          transaction.getTs()
              .getTime(), transaction.getProcessedTime()
          .getTime(), status, transaction.getAmount()
          .toString(), transaction.getSender(), transaction.getReceiver(),
          new TransactionDetails(sourceName,
              new TransactionDetails.Icon(TransactionDetails.Icon.Type.URL, transaction.getIcon()),
              transaction.getSku()), txType.equals(ETHER_TRANSFER) ? "ETH" : "APPC",
          mapOperations(transaction.getOperations())));
    }
    return transactionList;
  }

  private List<Operation> mapOperations(List<WalletHistory.Operation> operations) {
    List<Operation> list = new ArrayList<>(operations.size());

    for (WalletHistory.Operation operation : operations) {
      list.add(new Operation(operation.getTransactionId(), operation.getSender(),
          operation.getReceiver(), operation.getFee()));
    }
    return list;
  }

  @NotNull
  private Transaction.TransactionType mapTransactionType(WalletHistory.Transaction transaction) {
    switch (transaction.getType()) {
      case "Transfer OffChain":
        return TRANSFER_OFF_CHAIN;
      case "Topup OffChain":
        return TOP_UP;
      case "IAP OffChain":
        return IAP_OFFCHAIN;
      case "bonus":
        return BONUS;
      case "PoA OffChain":
        return ADS_OFFCHAIN;
      case "Ether Transfer":
        return ETHER_TRANSFER;
      case "IAP":
        return IAP;
      default:
        return STANDARD;
    }
  }
}
