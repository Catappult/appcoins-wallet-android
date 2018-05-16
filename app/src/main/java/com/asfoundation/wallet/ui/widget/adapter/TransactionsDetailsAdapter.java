package com.asfoundation.wallet.ui.widget.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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

public class TransactionsDetailsAdapter extends RecyclerView.Adapter<BinderViewHolder> {

  private final List<Operation> items = new ArrayList<>();
  private final OnMoreClickListener clickListener;

  private Wallet wallet;
  private NetworkInfo network;

  public TransactionsDetailsAdapter(OnMoreClickListener onTransactionClickListener) {
    this.clickListener = onTransactionClickListener;
  }

  @Override public BinderViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_transaction_details, parent, false);
    if (getItemCount() > 1) {
      int margin = (int) parent.getContext()
          .getResources()
          .getDimension(R.dimen.normal_margin) * 2;
      int marginNextItem = (int) parent.getContext()
          .getResources()
          .getDimension(R.dimen.half_large_margin);
      int parentWidth = parent.getWidth();
      int itemWidth = parentWidth - margin - marginNextItem;
      ViewGroup.LayoutParams params = view.getLayoutParams();
      params.width = itemWidth;
      view.setLayoutParams(params);
    }
    return new TransactionDetailsHolder(view, clickListener);
  }

  @Override public void onBindViewHolder(BinderViewHolder holder, int position) {
    Bundle addition = new Bundle();
    addition.putString(TransactionHolder.DEFAULT_ADDRESS_ADDITIONAL, wallet.address);
    holder.bind(items.get(position));
  }

  @Override public int getItemViewType(int position) {
    return 0;
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void setDefaultWallet(Wallet wallet) {
    this.wallet = wallet;
    notifyDataSetChanged();
  }

  public void setDefaultNetwork(NetworkInfo network) {
    this.network = network;
    notifyDataSetChanged();
  }

  public void addOperations(List<Operation> operations) {
    items.addAll(operations);
  }

  public void clear() {
    items.clear();
  }
}
