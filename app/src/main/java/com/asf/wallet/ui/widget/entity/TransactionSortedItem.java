package com.asf.wallet.ui.widget.entity;

import android.text.format.DateUtils;
import com.asf.wallet.entity.Transaction;
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
      return value.hash.equals(transaction.hash) && value.timeStamp == transaction.timeStamp;
    }
    return false;
  }

  @Override public boolean areItemsTheSame(SortedItem other) {
    return viewType == other.viewType;
  }

  @Override public Date getTimestamp() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(value.timeStamp * DateUtils.SECOND_IN_MILLIS);
    return calendar.getTime();
  }

  @Override public int compare(SortedItem other) {
    return
        other.viewType == viewType && ((TransactionSortedItem) other).value.hash.equalsIgnoreCase(
            value.hash) ? 0 : super.compare(other);
  }
}
