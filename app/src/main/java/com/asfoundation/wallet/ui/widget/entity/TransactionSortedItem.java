package com.asfoundation.wallet.ui.widget.entity;

import android.text.format.DateUtils;
import com.asfoundation.wallet.transactions.Transaction;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TransactionSortedItem extends TimestampSortedItem<Transaction> {

  public TransactionSortedItem(int viewType, Transaction value, int order) {
    super(viewType, value, 0, order);
  }

  @Override public boolean areContentsTheSame(SortedItem newItem) {
    if (viewType == newItem.viewType) {
      Transaction transaction = (Transaction) newItem.value;
      return value.getTransactionId()
          .equals(transaction.getTimeStamp()) && value.getTimeStamp() == transaction.getTimeStamp();
    }
    return false;
  }

  @Override public boolean areItemsTheSame(SortedItem other) {
    return viewType == other.viewType && ((TransactionSortedItem) other).value.getTransactionId()
        .equalsIgnoreCase(value.getTransactionId());
  }

  @Override public Date getTimestamp() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(value.getTimeStamp() * DateUtils.SECOND_IN_MILLIS);
    return calendar.getTime();
  }

  @Override public int compare(SortedItem other) {
    return other.viewType == viewType && ((TransactionSortedItem) other).value.getTransactionId()
        .equalsIgnoreCase(value.getTransactionId()) ? 0 : super.compare(other);
  }
}
