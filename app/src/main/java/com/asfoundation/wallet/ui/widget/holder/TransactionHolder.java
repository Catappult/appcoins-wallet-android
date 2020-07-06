package com.asfoundation.wallet.ui.widget.holder;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
import com.asfoundation.wallet.GlideApp;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.WalletCurrency;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import java.math.BigDecimal;

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
  private Resources resources;
  private CurrencyFormatUtils formatter;

  public TransactionHolder(int resId, ViewGroup parent, OnTransactionClickListener listener,
      Resources resources, CurrencyFormatUtils formatter) {
    super(resId, parent);

    srcImage = findViewById(R.id.img);
    typeIcon = findViewById(R.id.type_icon);
    address = findViewById(R.id.address);
    description = findViewById(R.id.description);
    value = findViewById(R.id.value);
    currency = findViewById(R.id.currency);
    status = findViewById(R.id.status);
    onTransactionClickListener = listener;
    this.formatter = formatter;

    itemView.setOnClickListener(this);
    this.resources = resources;
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

    String from = extractFrom(transaction);
    String to = extractTo(transaction);

    fill(from, to, currency, transaction.getValue(), ETHER_DECIMALS, transaction.getStatus(),
        transaction.getDetails());
  }

  private String extractTo(Transaction transaction) {
    if (transaction.getTo() != null) {
      return transaction.getTo();
    } else if (transaction.getOperations() != null
        && !transaction.getOperations()
        .isEmpty()
        && transaction.getOperations()
        .get(0) != null
        && transaction.getOperations()
        .get(0)
        .getTo() != null) {
      return transaction.getOperations()
          .get(0)
          .getTo();
    } else {
      return "";
    }
  }

  private String extractFrom(Transaction transaction) {
    if (transaction.getOperations() != null
        && !transaction.getOperations()
        .isEmpty()
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
        currencySymbol = WalletCurrency.CREDITS.getSymbol();
        break;
      case BONUS:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.ic_transaction_peer;
        currencySymbol = WalletCurrency.CREDITS.getSymbol();
        break;
      case TOP_UP:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.transaction_type_top_up;
        currencySymbol = WalletCurrency.CREDITS.getSymbol();
        break;
      case TRANSFER_OFF_CHAIN:
        typeIcon.setVisibility(View.GONE);
        transactionTypeIcon = R.drawable.transaction_type_transfer_off_chain;
        currencySymbol = WalletCurrency.CREDITS.getSymbol();
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
        address.setText(
            details.getSourceName() == null ? isSent ? to : from : getSourceText(transaction));
      }
      description.setText(details.getDescription() == null ? "" : details.getDescription());
    } else {
      address.setText(isSent ? to : from);
      description.setText("");
    }

    int finalTransactionTypeIcon = transactionTypeIcon;

    GlideApp.with(getContext())
        .load(uri)
        .apply(RequestOptions.bitmapTransform(new CircleCrop())
            .placeholder(finalTransactionTypeIcon)
            .error(transactionTypeIcon))
        .listener(new RequestListener<Drawable>() {

          @Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
              Target<Drawable> target, boolean isFirstResource) {
            typeIcon.setVisibility(View.GONE);
            return false;
          }

          @Override
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
              DataSource dataSource, boolean isFirstResource) {
            ((ImageView) typeIcon.findViewById(R.id.icon)).setImageResource(
                finalTransactionTypeIcon);
            return false;
          }
        })
        .into(srcImage);

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
      valueStr = (isSent ? "-" : "+") + getScaledValue(valueStr, decimals, currencySymbol);
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

  private String getScaledValue(String valueStr, long decimals, String currencySymbol) {
    WalletCurrency walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, decimals)));
    return formatter.formatCurrency(value, walletCurrency);
  }

  @Override public void onClick(View view) {
    onTransactionClickListener.onTransactionClick(view, transaction);
  }
}
