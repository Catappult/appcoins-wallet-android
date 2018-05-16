package com.asfoundation.wallet.ui.widget.holder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.ui.widget.OnMoreClickListener;

public class TransactionDetailsHolder extends BinderViewHolder<Operation>
    implements View.OnClickListener {

  public static final int VIEW_TYPE = 1003;
  public static final String DEFAULT_ADDRESS_ADDITIONAL = "default_address";
  public static final String DEFAULT_SYMBOL_ADDITIONAL = "network_symbol";
  private final TextView transactionId;
  private final TextView peerLabel;
  private final TextView peerAddress;
  private final TextView fee;

  private Operation operation;
  private OnMoreClickListener onTransactionClickListener;

  public TransactionDetailsHolder(View view, OnMoreClickListener listener) {
    super(view);

    transactionId = findViewById(R.id.transaction_id);
    peerLabel = findViewById(R.id.peer_addr_label);
    peerAddress = findViewById(R.id.peer_address);
    fee = findViewById(R.id.gas_fee);
    onTransactionClickListener = listener;

    //itemView.setOnClickListener(this);
  }

  @Override public void bind(@Nullable Operation data, @NonNull Bundle addition) {
    operation = data; // reset
    if (this.operation == null) {
      return;
    }
    String defaultAddress = addition.getString(DEFAULT_ADDRESS_ADDITIONAL);

    String peer = operation.getFrom();
    int peerLabel = R.string.label_from;
    if(peer.toLowerCase().equals(defaultAddress)) {
      peer = operation.getTo();
      peerLabel = R.string.label_to;
    }


    fill(operation.getTransactionId(), peerLabel, peer, operation.getFee());

  }

  private void fill(String transactionId, @StringRes int peerLabel, String peerAddress, String fee) {
    this.transactionId.setText(transactionId);
    this.peerLabel.setText(peerLabel);
    this.peerAddress.setText(peerAddress);
    this.fee.setText(fee);
  }

  @Override public void onClick(View view) {
    onTransactionClickListener.onTransactionClick(view, operation);
  }
}
