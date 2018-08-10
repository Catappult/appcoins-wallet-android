package com.asfoundation.wallet.ui.widget.holder;

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
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import com.asfoundation.wallet.widget.CircleTransformation;
import com.squareup.picasso.Picasso;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.asfoundation.wallet.C.ETHER_DECIMALS;

public class TransactionHolder extends BinderViewHolder<Transaction>
    implements View.OnClickListener {

  public static final int VIEW_TYPE = 1003;
  public static final String DEFAULT_ADDRESS_ADDITIONAL = "default_address";
  public static final String DEFAULT_SYMBOL_ADDITIONAL = "network_symbol";
  private final ImageView srcImage;
  private final View typeIcon;
  private final TextView address;
  private final TextView description;
  private final TextView value;
  private final TextView currency;
  private final TextView status;

  private Transaction transaction;
  private String defaultAddress;
  private OnTransactionClickListener onTransactionClickListener;

  public TransactionHolder(int resId, ViewGroup parent, OnTransactionClickListener listener) {
    super(resId, parent);

    srcImage = findViewById(R.id.img);
    typeIcon = findViewById(R.id.type_icon);
    address = findViewById(R.id.address);
    description = findViewById(R.id.description);
    value = findViewById(R.id.value);
    currency = findViewById(R.id.currency);
    status = findViewById(R.id.status);
    onTransactionClickListener = listener;

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

    fill(transaction.getType(), transaction.getFrom(), transaction.getTo(), currency,
        transaction.getValue(), ETHER_DECIMALS, transaction.getStatus(), transaction.getDetails());
  }

  private void fill(Transaction.TransactionType type, String from, String to, String currencySymbol,
      String valueStr, long decimals, Transaction.TransactionStatus transactionStatus,
      TransactionDetails details) {
    boolean isSent = from.toLowerCase()
        .equals(defaultAddress);

    int transactionTypeIcon = R.drawable.ic_transaction_peer;

    if (type == Transaction.TransactionType.ADS) {
      transactionTypeIcon = R.drawable.ic_transaction_poa;
    } else if (type == Transaction.TransactionType.IAB
        || type == Transaction.TransactionType.MICRO_IAB) {
      transactionTypeIcon = R.drawable.ic_transaction_iab;
    } else if (type == Transaction.TransactionType.OPEN_CHANNEL
        || type == Transaction.TransactionType.TOP_UP_CHANNEL
        || type == Transaction.TransactionType.CLOSE_CHANNEL) {
      transactionTypeIcon = R.drawable.ic_transaction_miu;
    }

    if (details == null) {
      srcImage.setImageResource(transactionTypeIcon);
      typeIcon.setVisibility(View.GONE);
    } else {
      Picasso.with(getContext())
          .load("file:" + details.getIcon())
          .transform(new CircleTransformation())
          .into(srcImage);
      ((ImageView) typeIcon.findViewById(R.id.icon)).setImageResource(transactionTypeIcon);
      typeIcon.setVisibility(View.VISIBLE);
    }

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


    switch (type) {
      case OPEN_CHANNEL:
        address.setText(R.string.miuraiden_trans_details_open);
        description.setText(isSent ? to : from);
        break;
      case TOP_UP_CHANNEL:
        address.setText(R.string.miuraiden_trans_details_topup);
        description.setText(isSent ? to : from);
        break;
      case CLOSE_CHANNEL:
        address.setText(R.string.miuraiden_trans_details_close);
        description.setText(isSent ? to : from);
        break;
      default:
        if (details != null) {
          address.setText(details.getSourceName());
          description.setText(details.getDescription());
        } else {
          address.setText(isSent ? to : from);
          description.setText("");
        }
        break;
    }

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
    onTransactionClickListener.onTransactionClick(view, transaction);
  }
}
