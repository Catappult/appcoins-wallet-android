package com.asfoundation.wallet.ui.widget.entity;

import com.asfoundation.wallet.ui.widget.holder.TransactionDateHolder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateSortedItem extends TimestampSortedItem<Date> {
  public DateSortedItem(Date value) {
    super(TransactionDateHolder.VIEW_TYPE, value, 0, DESC);
  }

  public static DateSortedItem round(long timeStampInSec) {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.setTimeInMillis(timeStampInSec);
    calendar.set(Calendar.MILLISECOND, 999);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    return new DateSortedItem(calendar.getTime());
  }

  @Override public Date getTimestamp() {
    return value;
  }

  @Override public boolean areContentsTheSame(SortedItem newItem) {
    return viewType == newItem.viewType && value.equals(((TimestampSortedItem) newItem).value);
  }

  @Override public boolean areItemsTheSame(SortedItem other) {
    return viewType == other.viewType;
  }
}
