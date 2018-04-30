package com.asfoundation.wallet.ui.widget.holder;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionOperation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.C.ETHER_DECIMALS;

public class TransactionHolder extends BinderViewHolder<Transaction>
    implements View.OnClickListener {

  public static final int VIEW_TYPE = 1003;
  public static final String DEFAULT_ADDRESS_ADDITIONAL = "default_address";
  public static final String DEFAULT_SYMBOL_ADDITIONAL = "network_symbol";
  private static final int SIGNIFICANT_FIGURES = 3;
  private final ImageView srcImage;
  private final ImageView typeIcon;
  private final TextView address;
  private final TextView description;
  private final TextView value;
  private final TextView currency;
  private final TextView status;

  private Transaction transaction;
  private String defaultAddress;
  private OnTransactionClickListener onTransactionClickListener;

  public TransactionHolder(int resId, ViewGroup parent) {
    super(resId, parent);

    srcImage = findViewById(R.id.img);
    typeIcon = findViewById(R.id.icon);
    address = findViewById(R.id.address);
    description = findViewById(R.id.description);
    value = findViewById(R.id.value);
    currency = findViewById(R.id.currency);
    status = findViewById(R.id.status);

    itemView.setOnClickListener(this);
  }

  @Override public void bind(@Nullable Transaction data, @NonNull Bundle addition) {
    transaction = data; // reset
    if (this.transaction == null) {
      return;
    }
    defaultAddress = addition.getString(DEFAULT_ADDRESS_ADDITIONAL);

    String currency = addition.getString(DEFAULT_SYMBOL_ADDITIONAL);

    if (!TextUtils.isEmpty(transaction.getCurrency())) {
      currency = transaction.getCurrency();
    }
    // If operations include token transfer, display token transfer instead
    //TransactionOperation operation =
    //    transaction.operations == null || transaction.operations.length == 0 ? null
    //        : transaction.operations[0];

    //if (operation == null || operation.contract == null) {
      // default to ether transaction
      fill(transaction.getType(), transaction.getFrom(), transaction.getTo(), currency,
          transaction.getValue(), ETHER_DECIMALS, transaction.getStatus(), transaction.getDetails());
    //} else {
    //  fill(transaction.error, operation.from, operation.to, operation.contract.symbol,
    //      operation.value, operation.contract.decimals, transaction.timeStamp);
    //}
  }

  private void fill(Transaction.TransactionType type, String from, String to, String currencySymbol,
      String valueStr, long decimals, Transaction.TransactionStatus transactionStatus,
      String details) {
    boolean isSent = from.toLowerCase()
        .equals(defaultAddress);

    int transactionTypeIcon = R.drawable.ic_transaction_peer;

    if (type == Transaction.TransactionType.ADS) {
      transactionTypeIcon = R.drawable.ic_transaction_poa;
    } else if (type == Transaction.TransactionType.IAB) {
      transactionTypeIcon = R.drawable.ic_transaction_iab;
    }

    srcImage.setImageResource(transactionTypeIcon);

    int statusText = R.string.transaction_status_success;
    int statusColor = R.color.green;

    switch (transactionStatus) {
      case PENDING:
        statusText = R.string.transaction_status_pending;
        statusColor = R.color.orange;
        break;
      case FAILED:
        statusText = R.string.transaction_status_failed;
        statusColor = R.color.red;
        break;
    }

    status.setText(statusText);
    status.setTextColor(ContextCompat.getColor(getContext(), statusColor));

    address.setText(isSent ? to : from);
    description.setText(details);
    if (valueStr.equals("0")) {
      valueStr = "0 ";
    } else {
      valueStr = (isSent ? "-" : "+") + getScaledValue(valueStr, decimals);
    }

    currency.setText(currencySymbol);

    this.value.setText(valueStr);
  }

  private String getScaledValue(String valueStr, long decimals) {
    // Perform decimal conversion
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, decimals)));
    int scale = 4; //SIGNIFICANT_FIGURES - value.precision() + value.scale();
    return value.setScale(scale, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
  }

  @Override public void onClick(View view) {
    if (onTransactionClickListener != null) {
      onTransactionClickListener.onTransactionClick(view, transaction.getTransaction());
    }
  }

  public void setOnTransactionClickListener(OnTransactionClickListener onTransactionClickListener) {
    this.onTransactionClickListener = onTransactionClickListener;
  }
}
