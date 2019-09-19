package com.asfoundation.wallet.ui.widget.adapter;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.referrals.ReferralNotification;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import com.asfoundation.wallet.ui.widget.entity.DateSortedItem;
import com.asfoundation.wallet.ui.widget.entity.SortedItem;
import com.asfoundation.wallet.ui.widget.entity.TimestampSortedItem;
import com.asfoundation.wallet.ui.widget.entity.TransactionSortedItem;
import com.asfoundation.wallet.ui.widget.holder.AppcoinsApplicationListViewHolder;
import com.asfoundation.wallet.ui.widget.holder.BinderViewHolder;
import com.asfoundation.wallet.ui.widget.holder.ReferralNotificationAction;
import com.asfoundation.wallet.ui.widget.holder.ReferralNotificationsListViewHolder;
import com.asfoundation.wallet.ui.widget.holder.TransactionDateHolder;
import com.asfoundation.wallet.ui.widget.holder.TransactionHolder;
import java.util.Collections;
import java.util.List;
import rx.functions.Action1;
import rx.functions.Action2;

public class TransactionsAdapter extends RecyclerView.Adapter<BinderViewHolder> {

  private final SortedList<SortedItem> items =
      new SortedList<>(SortedItem.class, new SortedList.Callback<SortedItem>() {
        @Override public int compare(SortedItem left, SortedItem right) {
          return left.compare(right);
        }

        @Override public void onChanged(int position, int count) {
          notifyItemRangeChanged(position, count);
        }

        @Override public boolean areContentsTheSame(SortedItem oldItem, SortedItem newItem) {
          return oldItem.areContentsTheSame(newItem);
        }

        @Override public boolean areItemsTheSame(SortedItem left, SortedItem right) {
          return left.areItemsTheSame(right);
        }

        @Override public void onInserted(int position, int count) {
          notifyItemRangeInserted(position, count);
        }

        @Override public void onRemoved(int position, int count) {
          notifyItemRangeRemoved(position, count);
        }

        @Override public void onMoved(int fromPosition, int toPosition) {
          notifyItemMoved(fromPosition, toPosition);
        }
      });
  private final OnTransactionClickListener onTransactionClickListener;
  private final Action1<AppcoinsApplication> applicationClickListener;
  private final Action2<ReferralNotification, ReferralNotificationAction>
      referralNotificationClickListener;

  private Wallet wallet;
  private NetworkInfo network;

  public TransactionsAdapter(OnTransactionClickListener onTransactionClickListener,
      Action1<AppcoinsApplication> applicationClickListener,
      Action2<ReferralNotification, ReferralNotificationAction> referralNotificationClickListener) {
    this.onTransactionClickListener = onTransactionClickListener;
    this.applicationClickListener = applicationClickListener;
    this.referralNotificationClickListener = referralNotificationClickListener;
  }

  @Override public BinderViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
    BinderViewHolder holder = null;
    switch (viewType) {
      case TransactionHolder.VIEW_TYPE:
        holder =
            new TransactionHolder(R.layout.item_transaction, parent, onTransactionClickListener);
        break;
      case TransactionDateHolder.VIEW_TYPE:
        holder = new TransactionDateHolder(R.layout.item_transactions_date_head, parent);
        break;
      case AppcoinsApplicationListViewHolder.VIEW_TYPE:
        holder =
            new AppcoinsApplicationListViewHolder(R.layout.item_appcoins_application_list, parent,
                applicationClickListener);
        break;
      case ReferralNotificationsListViewHolder.VIEW_TYPE:
        holder = new ReferralNotificationsListViewHolder(R.layout.item_referral_notifications_list,
            parent, referralNotificationClickListener);
        break;
    }
    return holder;
  }

  @Override public void onBindViewHolder(BinderViewHolder holder, int position) {
    Bundle addition = new Bundle();
    addition.putString(TransactionHolder.DEFAULT_ADDRESS_ADDITIONAL, wallet.address);
    addition.putString(TransactionHolder.DEFAULT_SYMBOL_ADDITIONAL, network.symbol);
    holder.bind(items.get(position).value, addition);
  }

  @Override public int getItemViewType(int position) {
    return items.get(position).viewType;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public int getTransactionsCount() {
    int counter = 0;
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i) instanceof TransactionSortedItem) {
        counter++;
      }
    }
    return counter;
  }

  public int getNotificationsCount() {
    int counter = 0;
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i) instanceof ReferralNotificationSortedItem) {
        counter += ((ReferralNotificationSortedItem) items.get(i)).value.size();
      }
    }
    return counter;
  }

  public void setDefaultWallet(Wallet wallet) {
    this.wallet = wallet;
    notifyDataSetChanged();
  }

  public void setDefaultNetwork(NetworkInfo network) {
    this.network = network;
    notifyDataSetChanged();
  }

  public void addTransactions(List<Transaction> transactions) {
    items.beginBatchedUpdates();
    if (items.size() == 0) {
      items.add(new ApplicationSortedItem(Collections.emptyList(),
          AppcoinsApplicationListViewHolder.VIEW_TYPE));
    }
    for (Transaction transaction : transactions) {
      TransactionSortedItem sortedItem =
          new TransactionSortedItem(TransactionHolder.VIEW_TYPE, transaction,
              TimestampSortedItem.DESC);
      items.add(sortedItem);
      items.add(DateSortedItem.round(transaction.getTimeStamp()));
    }
    items.endBatchedUpdates();
  }

  public void clear() {
    items.clear();
  }

  public void setApps(List<AppcoinsApplication> apps) {
    items.add(new ApplicationSortedItem(apps, AppcoinsApplicationListViewHolder.VIEW_TYPE));
  }

  public void setNotifications(List<ReferralNotification> notifications) {
    items.add(new ReferralNotificationSortedItem(notifications,
        ReferralNotificationsListViewHolder.VIEW_TYPE));
  }
}
