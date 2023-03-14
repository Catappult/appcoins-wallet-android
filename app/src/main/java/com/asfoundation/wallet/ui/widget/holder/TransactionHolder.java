package com.asfoundation.wallet.ui.widget.holder;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.asf.wallet.R;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.GlideApp;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.Transaction.TransactionType;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.widget.OnTransactionClickListener;
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils;
import com.appcoins.wallet.core.utils.common.WalletCurrency;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionHolder extends BinderViewHolder<Transaction>
    implements View.OnClickListener {

  public static final int VIEW_TYPE = 1003;
  public static final String DEFAULT_ADDRESS_ADDITIONAL = "default_address";
  public static final String DEFAULT_SYMBOL_ADDITIONAL = "network_symbol";
  private final ImageView srcImage;
  private final View typeIcon;
  private final TextView address;
  private final TextView description;
  private final TextView paidValue;
  private final TextView paidCurrency;
  private final TextView value;
  private final TextView currency;
  private final OnTransactionClickListener onTransactionClickListener;
  private final CurrencyFormatUtils formatter;
  private final TextView revertMessage;
  private Transaction transaction;
  private String defaultAddress;

  public TransactionHolder(int resId, ViewGroup parent, OnTransactionClickListener listener,
      CurrencyFormatUtils formatter) {
    super(resId, parent);

    srcImage = findViewById(R.id.img);
    typeIcon = findViewById(R.id.type_icon);
    address = findViewById(R.id.address);
    description = findViewById(R.id.description);
    paidValue = findViewById(R.id.paid_value);
    paidCurrency = findViewById(R.id.paid_currency);
    value = findViewById(R.id.value);
    currency = findViewById(R.id.currency);
    revertMessage = findViewById(R.id.revert_message);
    onTransactionClickListener = listener;
    this.formatter = formatter;

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

    fill(transaction.getFrom(), transaction.getTo(), currency, transaction.getValue(),
        transaction.getPaidAmount(), transaction.getPaidCurrency(), transaction.getDetails());
  }

  private void fill(String from, String to, String currencySymbol, String valueStr,
      String paidAmount, String paidCurrency, TransactionDetails details) {
    boolean isSent = from.toLowerCase()
        .equals(defaultAddress);

    revertMessage.setVisibility(View.GONE);

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
      case IAP:
      case IAP_OFFCHAIN:
        transactionTypeIcon = R.drawable.ic_transaction_iab;
        setTypeIconVisibilityBasedOnDescription(details, uri);
        break;
      case BONUS_REVERT:
      case TOP_UP_REVERT:
        transactionTypeIcon = R.drawable.ic_transaction_revert;
        typeIcon.setVisibility(View.VISIBLE);
        setRevertMessage();
        currencySymbol = WalletCurrency.CREDITS.getSymbol();
        break;
      case IAP_REVERT:
        transactionTypeIcon = R.drawable.ic_transaction_revert;
        typeIcon.setVisibility(View.VISIBLE);
        setRevertMessage();
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
      case SUBS_OFFCHAIN:
        transactionTypeIcon = R.drawable.ic_transaction_subscription;
        setTypeIconVisibilityBasedOnDescription(details, uri);
        currencySymbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      default:
        transactionTypeIcon = R.drawable.ic_transaction_peer;
        setTypeIconVisibilityBasedOnDescription(details, uri);
    }

    if (details != null) {
      if (transaction.getType()
          .equals(TransactionType.BONUS)) {
        address.setText(R.string.transaction_type_bonus);
      } else if (transaction.getType()
          .equals(TransactionType.TOP_UP)) {
        address.setText(R.string.topup_home_button);
      } else if (transaction.getType()
          .equals(TransactionType.TRANSFER_OFF_CHAIN)) {
        address.setText(R.string.transaction_type_p2p);
      } else if (transaction.getType()
          .equals(TransactionType.TOP_UP_REVERT)) {
        address.setText(R.string.transaction_type_reverted_topup_title);
      } else if (transaction.getType()
          .equals(TransactionType.BONUS_REVERT)) {
        address.setText(R.string.transaction_type_reverted_bonus_title);
      } else if (transaction.getType()
          .equals(TransactionType.IAP_REVERT)) {
        address.setText(R.string.transaction_type_reverted_purchase_title);
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
            ImageView icon = typeIcon.findViewById(R.id.icon);
            icon.setImageResource(finalTransactionTypeIcon);
            icon.setVisibility(View.VISIBLE);
            return false;
          }
        })
        .into(srcImage);

    if (valueStr.equals("0")) {
      valueStr = "0 ";
    } else if (transaction.getType() == TransactionType.IAP_REVERT) {
      valueStr = getScaledValue(valueStr, C.ETHER_DECIMALS, currencySymbol);
    } else {
      valueStr = (isSent ? "-" : "+") + getScaledValue(valueStr, C.ETHER_DECIMALS, currencySymbol);
    }

    if (shouldShowFiat(paidAmount, paidCurrency)) {
      paidAmount = (isSent ? "-" : "+") + getScaledValue(paidAmount, 0, "");
      this.paidValue.setText(paidAmount);
      this.paidCurrency.setText(paidCurrency);
      this.value.setVisibility(View.VISIBLE);
      this.currency.setVisibility(View.VISIBLE);
      this.value.setText(valueStr);
      this.currency.setText(currencySymbol);
    } else {
      this.paidValue.setText(valueStr);
      this.paidCurrency.setText(currencySymbol);
      this.value.setVisibility(View.GONE);
      this.currency.setVisibility(View.GONE);
    }
  }

  private boolean shouldShowFiat(String paidAmount, String paidCurrency) {
    return paidAmount != null
        && !paidCurrency.equals("APPC")
        && !paidCurrency.equals("APPC-C")
        && !paidCurrency.equals("ETH");
  }

  private void setRevertMessage() {
    String message = null;
    List<Transaction> links = transaction.getLinkedTx();
    if (links == null || links.isEmpty()) {
      revertMessage.setVisibility(View.GONE);
    } else {
      Transaction linkedTx = links.get(0);
      if (transaction.getType() == TransactionType.BONUS_REVERT) {
        message = getString(R.string.transaction_type_reverted_bonus_body,
            getDate(linkedTx.getTimeStamp()));
      } else if (transaction.getType() == TransactionType.IAP_REVERT) {
        message = getString(R.string.transaction_type_reverted_purchase_body,
            getDate(linkedTx.getTimeStamp()));
      } else if (transaction.getType() == TransactionType.TOP_UP_REVERT) {
        message = getString(R.string.transaction_type_reverted_topup_body,
            getDate(linkedTx.getTimeStamp()));
      }

      revertMessage.setText(message);
      revertMessage.setVisibility(View.VISIBLE);
    }
  }

  private String getDate(long timeStampInSec) {
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTimeInMillis(timeStampInSec);
    return DateFormat.format("MMM, dd yyyy", cal.getTime())
        .toString();
  }

  private String getSourceText(Transaction transaction) {
    if (transaction.getType()
        .equals(TransactionType.BONUS)) {
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

  private boolean isRevertedType(TransactionType type) {
    return type == TransactionType.BONUS_REVERT
        || type == TransactionType.IAP_REVERT
        || type == TransactionType.TOP_UP_REVERT;
  }
}
