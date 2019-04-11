package com.asfoundation.wallet.ui.widget.holder;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.asf.wallet.R;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import com.asfoundation.wallet.widget.CircleTransformation;
import com.squareup.picasso.Callback;
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

    String to = extractTo(transaction);
    String from = extractFrom(transaction);

    fill(from, to, currency, transaction.getValue(), ETHER_DECIMALS, transaction.getStatus(),
        transaction.getDetails());
  }

  private String extractTo(Transaction transaction) {
    if (transaction.getOperations() != null
        && transaction.getOperations()
        .get(0) != null
        && transaction.getOperations()
        .get(0)
        .getTo() != null) {
      return transaction.getOperations()
          .get(0)
          .getTo();
    } else {
      return transaction.getTo();
    }
  }

  private String extractFrom(Transaction transaction) {
    if (transaction.getOperations() != null
        && transaction.getOperations()
        .get(0) != null
        && transaction.getOperations()
        .get(0)
        .getFrom() != null) {
      return transaction.getOperations()
          .get(0)
          .getFrom();
    } else {
      return transaction.getFrom();
    }
  }

  private void fill(String from, String to, String currencySymbol, String valueStr, long decimals,
      Transaction.TransactionStatus transactionStatus, TransactionDetails details) {
    boolean isSent = from.toLowerCase()
        .equals(defaultAddress);

    TransactionDetails.Icon icon;
    String uri = null;
    if (details != null) {
      icon = details.getIcon();
      switch (icon.getType()) {
        case FILE:
          uri = "file:" + icon.getUri();
          break;
        case URL:
          uri = icon.getUri();
          break;
      }
    }

    int transactionTypeIcon;
    switch (transaction.getType()) {
      case IAB:
      case IAP_OFFCHAIN:
        transactionTypeIcon = R.drawable.ic_transaction_iab;
        setTypeIconVisibilityBasedOnDescription(details, uri);
        break;
      case ADS:
      case ADS_OFFCHAIN:
        transactionTypeIcon = R.drawable.ic_transaction_poa;
        setTypeIconVisibilityBasedOnDescription(details, uri);
        currencySymbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case BONUS:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.ic_transaction_peer;
        currencySymbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case TOP_UP:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.transaction_type_top_up;
        currencySymbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case TRANSFER_OFF_CHAIN:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.transaction_type_transfer_off_chain;
        currencySymbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      default:
        transactionTypeIcon = R.drawable.ic_transaction_peer;
        setTypeIconVisibilityBasedOnDescription(details, uri);
    }

    if (details != null) {
      if (transaction.getType()
          .equals(Transaction.TransactionType.BONUS)) {
        address.setText(R.string.transaction_type_bonus);
      } else if (transaction.getType()
          .equals(Transaction.TransactionType.TOP_UP)) {
        address.setText(R.string.topup_home_button);
      } else if (transaction.getType()
          .equals(Transaction.TransactionType.TRANSFER_OFF_CHAIN)) {
        address.setText(R.string.transaction_type_p2p);
      } else {
        address.setText(details.getSourceName() == null ? isSent ? to : from : getSourceText(transaction));
      }
      description.setText(details.getDescription() == null ? "" : details.getDescription());
    } else {
      address.setText(isSent ? to : from);
      description.setText("");
    }

    int finalTransactionTypeIcon = transactionTypeIcon;
    Picasso.with(getContext())
        .load(uri)
        .transform(new CircleTransformation())
        .placeholder(finalTransactionTypeIcon)
        .error(transactionTypeIcon)
        .fit()
        .into(srcImage, new Callback() {
          @Override public void onSuccess() {
            ((ImageView) typeIcon.findViewById(R.id.icon)).setImageResource(
                finalTransactionTypeIcon);
          }

          @Override public void onError() {
            typeIcon.setVisibility(View.GONE);
          }
        });

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

    if (valueStr.equals("0")) {
      valueStr = "0 ";
    } else {
      valueStr = (isSent ? "-" : "+") + getScaledValue(valueStr, decimals);
    }

    currency.setText(currencySymbol);

    this.value.setText(valueStr);
  }

  private String getSourceText(Transaction transaction) {
    if (transaction.getType()
        .equals(Transaction.TransactionType.BONUS)) {
      return getContext().getString(R.string.gamification_transaction_title,
          transaction.getDetails()
              .getSourceName());
    }
    return transaction.getDetails()
        .getSourceName();
  }

  private void setTypeIconVisibilityBasedOnDescription(TransactionDetails details, String uri) {
    if (uri == null || details.getSourceName() == null) {
      typeIcon.setVisibility(View.GONE);
    } else {
      typeIcon.setVisibility(View.VISIBLE);
    }
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
