package com.asfoundation.wallet.ui.widget.holder;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.appcoins.wallet.core.utils.properties.MiscProperties;
import com.asf.wallet.R.id;
import com.asf.wallet.R.string;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.ui.widget.OnMoreClickListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.ui.widget.holder.TransactionHolder.DEFAULT_SYMBOL_ADDITIONAL;

/**
 * Transaction details view holder used to show details about all transfer steps of specific
 * transaction.
 */
public class TransactionDetailsHolder extends BinderViewHolder<Operation>
    implements OnClickListener {

  private static final int DEFAULT_SCALE = 8;
  /** Tag used to obtain wallet address in used */
  private static final String DEFAULT_ADDRESS_ADDITIONAL = "default_address";
  /** The transaction operation item view */
  private final View itemView;
  /** The operation transaction id */
  private final TextView transactionId;
  /** the peer label to identify if it is a receiver or a sender */
  private final TextView peerLabel;
  /** The peer address */
  private final TextView peerAddress;
  /** The fee of the operation */
  private final TextView fee;
  /** The button to see more details about the transfer */
  private final Button more;
  /** The listener for the more button click */
  private final OnMoreClickListener onMoreClickListener;
  /** The operation object to be shown */
  private Operation operation;

  public TransactionDetailsHolder(View view, OnMoreClickListener listener) {
    super(view);
    itemView = view;
    transactionId = findViewById(id.transaction_id);
    peerLabel = findViewById(id.peer_addr_label);
    peerAddress = findViewById(id.peer_address);
    fee = findViewById(id.gas_fee);
    more = findViewById(id.more_detail);
    onMoreClickListener = listener;
  }

  @Override public void bind(@Nullable Operation data, @NonNull Bundle addition) {
    operation = data;
    if (this.operation == null) {
      return;
    }
    String defaultAddress = addition.getString(DEFAULT_ADDRESS_ADDITIONAL);

    String currency = addition.getString(DEFAULT_SYMBOL_ADDITIONAL);

    String peer = operation.getFrom();
    int peerLabel = string.label_from;
    // Check if the from matches the current wallet address, ifo so then we change a label to "To"
    if (peer.toLowerCase()
        .equals(defaultAddress)) {
      peer = operation.getTo();
      peerLabel = string.label_to;
    }

    more.setOnClickListener(this);

    fill(operation.getTransactionId(), peerLabel, peer, formatFee() + " " + currency.toUpperCase());
  }

  private String formatFee() {
    int decimals = MiscProperties.INSTANCE.getDEFAULT_TOKEN_DECIMALS();

    return new BigDecimal(operation.getFee()).divide(BigDecimal.valueOf(Math.pow(10.0, decimals)),
            DEFAULT_SCALE, RoundingMode.HALF_UP)
        .toPlainString();
  }

  private void fill(String transactionId, @StringRes int peerLabel, String peerAddress,
      String fee) {
    this.transactionId.setText(transactionId);
    this.peerLabel.setText(peerLabel);
    this.peerAddress.setText(peerAddress);
    this.fee.setText(fee);
  }

  @Override public void onClick(View view) {
    onMoreClickListener.onTransactionClick(view, operation);
  }
}
