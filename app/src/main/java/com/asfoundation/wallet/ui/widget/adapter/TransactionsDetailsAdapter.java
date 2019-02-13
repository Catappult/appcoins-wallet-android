package com.asfoundation.wallet.ui.widget.adapter;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.ui.widget.OnMoreClickListener;
import com.asfoundation.wallet.ui.widget.holder.BinderViewHolder;
import com.asfoundation.wallet.ui.widget.holder.TransactionDetailsHolder;
import com.asfoundation.wallet.ui.widget.holder.TransactionHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction details adapter, used to list the several operations part of the transaction.
 */
public class TransactionsDetailsAdapter extends RecyclerView.Adapter<BinderViewHolder> {

  /** List of operations on the transaction */
  private final List<Operation> items = new ArrayList<>();
  /** The listener for the more button click */
  private final OnMoreClickListener clickListener;
  /** The current wallet in use */
  private Wallet wallet;
  private NetworkInfo network;

  public TransactionsDetailsAdapter(OnMoreClickListener onTransactionClickListener) {
    this.clickListener = onTransactionClickListener;
  }

  @Override public BinderViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
    View item = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_transaction_details, parent, false);
    // If we have details with more than one operation we change the width of the card containing
    // the operation details so the user knows that there are more than one operation detail in the
    // transaction
    if (getItemCount() > 1) {
      int itemWidth = (int) parent.getResources()
          .getDimension(R.dimen.transaction_details_width);
      ViewGroup.LayoutParams params = item.getLayoutParams();
      params.width = itemWidth;
      item.setLayoutParams(params);
    }
    return new TransactionDetailsHolder(item, clickListener);
  }

  @Override public void onBindViewHolder(BinderViewHolder holder, int position) {
    if (network != null) {
      Bundle addition = new Bundle();
      addition.putString(TransactionHolder.DEFAULT_ADDRESS_ADDITIONAL, wallet.address);
      addition.putString(TransactionHolder.DEFAULT_SYMBOL_ADDITIONAL, network.symbol);
      holder.bind(items.get(position), addition);
    }
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  /**
   * Set the default wallet so we can check what is the current wallet in use
   *
   * @param wallet The wallet object containing the current wallet information.
   */
  public void setDefaultWallet(Wallet wallet) {
    this.wallet = wallet;
    notifyDataSetChanged();
  }

  public void setDefaultNetwork(NetworkInfo network) {
    this.network = network;
    notifyDataSetChanged();
  }

  /**
   * Method to add the operations list for the given transaction.
   *
   * @param operations the list f operations of the transaction.
   */
  public void addOperations(List<Operation> operations) {
    clear();
    items.addAll(operations);
  }

  /**
   * Method to clear the operations list.
   */
  public void clear() {
    items.clear();
  }
}
