package com.asfoundation.wallet.ui.balance;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asfoundation.wallet.GlideApp;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TransactionsDetailsModel;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.BaseActivity;
import com.asfoundation.wallet.ui.common.SeparatorView;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsDetailsAdapter;
import com.asfoundation.wallet.util.BalanceUtils;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.WalletCurrency;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModel;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.Key.GLOBAL_BALANCE_CURRENCY;
import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailActivity extends BaseActivity {

  private static final int DECIMALS = 18;
  @Inject TransactionDetailViewModelFactory transactionDetailViewModelFactory;
  @Inject CurrencyFormatUtils formatter;
  private TransactionDetailViewModel viewModel;
  private Transaction transaction;
  private String globalBalanceCurrency;
  private boolean isSent = false;
  private TextView amount;
  private TextView paidAmount;
  private TextView localFiatAmount;
  private SeparatorView verticalSeparator;
  private TransactionsDetailsAdapter adapter;
  private RecyclerView detailsList;
  private Dialog dialog;
  private CompositeDisposable disposables;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AndroidInjection.inject(this);

    setContentView(R.layout.activity_transaction_detail);
    findViewById(R.id.more_detail).setVisibility(View.GONE);

    disposables = new CompositeDisposable();
    transaction = getIntent().getParcelableExtra(TRANSACTION);
    if (transaction == null) {
      finish();
      return;
    }
    toolbar();

    globalBalanceCurrency = getIntent().getStringExtra(GLOBAL_BALANCE_CURRENCY);
    ((ToolbarArcBackground) findViewById(R.id.toolbar_background_arc)).setScale(1f);

    amount = findViewById(R.id.amount);
    paidAmount = findViewById(R.id.paid_amount);
    localFiatAmount = findViewById(R.id.local_fiat_amount);
    verticalSeparator = findViewById(R.id.vertical_separator);
    adapter = new TransactionsDetailsAdapter(this::onMoreClicked);
    detailsList = findViewById(R.id.details_list);
    detailsList.setAdapter(adapter);

    viewModel = ViewModelProviders.of(this, transactionDetailViewModelFactory)
        .get(TransactionDetailViewModel.class);

    viewModel.initializeView(getPaidValue(), transaction.getPaidCurrency(), globalBalanceCurrency);
    viewModel.transactionsDetailsModel()
        .observe(this, this::onTransactionsDetails);

    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          float percentage =
              1 - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
          findViewById(R.id.img).setScaleX(percentage);
          findViewById(R.id.img).setScaleY(percentage);
        });
  }

  @Override protected void onResume() {
    super.onResume();
    sendPageViewEvent();
  }

  @Override protected void onStop() {
    super.onStop();
    disposables.dispose();
    hideDialog();
  }

  private void onTransactionsDetails(TransactionsDetailsModel transactionsDetailsModel) {
    adapter.setDefaultWallet(transactionsDetailsModel.getWallet());
    adapter.setDefaultNetwork(transactionsDetailsModel.getNetworkInfo());

    if (transaction.getOperations() != null && !transaction.getOperations()
        .isEmpty()) {
      adapter.addOperations(transaction.getOperations());
      detailsList.setVisibility(View.VISIBLE);
    }

    isSent = transaction.getFrom()
        .toLowerCase()
        .equals(transactionsDetailsModel.getWallet().address);

    String symbol =
        transaction.getCurrency() == null ? (transactionsDetailsModel.getNetworkInfo() == null ? ""
            : transactionsDetailsModel.getNetworkInfo().symbol) : transaction.getCurrency();

    String icon = null;
    String id = null;
    String description = null;
    String to = null;
    TransactionDetails details = transaction.getDetails();

    if (details != null) {
      icon = details.getIcon()
          .getUri();
      id = details.getSourceName();
      description = details.getDescription();
    }

    @StringRes int statusStr = R.string.transaction_status_success;
    @ColorRes int statusColor = R.color.green;
    @ColorRes int descriptionColor = R.color.transaction_orange;

    @StringRes int typeStr = R.string.transaction_type_standard;
    @DrawableRes int typeIcon = R.drawable.ic_transaction_peer;
    View button = findViewById(R.id.more_detail);
    View manageSubscriptions = findViewById(R.id.manage_subscriptions);
    View categoryBackground = findViewById(R.id.category_icon_background);

    boolean isRevertTransaction = isRevertTransaction(transaction);
    boolean isRevertedTransaction = isRevertedTransaction(transaction);
    @StringRes int revertedDescription = -1;

    switch (transaction.getType()) {
      case ADS:
        typeStr = R.string.transaction_type_poa;
        typeIcon = R.drawable.ic_transaction_poa;
        manageSubscriptions.setVisibility(View.GONE);
        break;
      case ADS_OFFCHAIN:
        typeStr = R.string.transaction_type_poa_offchain;
        typeIcon = R.drawable.ic_transaction_poa;
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case IAP_REVERT:
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
        revertedDescription = R.string.transaction_type_reverted_purchase_title;
        descriptionColor = R.color.green;
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        break;
      case IAP_OFFCHAIN:
        button.setVisibility(View.VISIBLE);
        to = transaction.getTo();
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
        if (isRevertedTransaction) {
          revertedDescription = R.string.transaction_type_reverted_purchase_title;
        }
        break;
      case BONUS_REVERT:
        typeStr = R.string.transaction_type_bonus;
        typeIcon = -1;
        id = getString(R.string.transaction_type_reverted_bonus_title);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        break;
      case BONUS:
        button.setVisibility(View.VISIBLE);
        typeStr = R.string.transaction_type_bonus;
        typeIcon = -1;
        if (transaction.getDetails()
            .getSourceName() == null) {
          id = getString(R.string.transaction_type_bonus);
        } else {
          id = getString(R.string.gamification_level_bonus, transaction.getDetails()
              .getSourceName());
        }
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
        symbol = getString(R.string.p2p_send_currency_appc_c);
        if (isRevertedTransaction) {
          revertedDescription = R.string.transaction_type_reverted_bonus_title;
        }
        break;
      case TOP_UP_REVERT:
        categoryBackground.setBackground(null);
        typeStr = R.string.topup_title;
        typeIcon = R.drawable.transaction_type_top_up;
        id = getString(R.string.transaction_type_reverted_topup_title);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        break;
      case TOP_UP:
        typeStr = R.string.topup_title;
        id = getString(R.string.topup_title);
        categoryBackground.setBackground(null);
        typeIcon = R.drawable.transaction_type_top_up;
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
        symbol = getString(R.string.p2p_send_currency_appc_c);
        if (isRevertedTransaction) {
          revertedDescription = R.string.transaction_type_reverted_topup_title;
        }
        break;
      case TRANSFER_OFF_CHAIN:
        typeStr = R.string.transaction_type_p2p;
        id = isSent ? "Transfer Sent" : getString(R.string.askafriend_received_title);
        typeIcon = R.drawable.transaction_type_transfer_off_chain;
        categoryBackground.setBackground(null);
        to = transaction.getTo();
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case SUBS_OFFCHAIN:
        typeStr = R.string.subscriptions_category_title;
        typeIcon = R.drawable.ic_transaction_subscription;
        categoryBackground.setBackground(null);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.VISIBLE);
        manageSubscriptions.setOnClickListener(
            view -> viewModel.showManageSubscriptions(view.getContext()));
        to = transaction.getTo();
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
    }

    switch (transaction.getStatus()) {
      case FAILED:
        statusStr = R.string.transaction_status_failed;
        statusColor = R.color.red;
        break;
      case PENDING:
        statusStr = R.string.transaction_status_pending;
        statusColor = R.color.transaction_orange;
        break;
    }

    if (isRevertedTransaction) {
      statusStr = R.string.transaction_status_reverted;
      statusColor = R.color.orange;
    } else if (isRevertTransaction) {
      statusColor = R.color.green;
    }

    String localFiatCurrency = globalBalanceCurrency;

    setUiContent(transaction.getTimeStamp(), getValue(symbol), symbol, getPaidValue(),
        transaction.getPaidCurrency(), transactionsDetailsModel.getFiatValue()
            .getAmount()
            .toString(), localFiatCurrency, icon, id, description, typeStr, typeIcon, statusStr,
        statusColor, to, isSent, isRevertTransaction, isRevertedTransaction, revertedDescription,
        descriptionColor);
  }

  private String getScaledValue(String valueStr, long decimals, String currencySymbol) {
    WalletCurrency walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, decimals)));
    return formatter.formatCurrency(value, walletCurrency);
  }

  private String getDateAndTime(long timeStampInSec) {
    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    cal.setTimeInMillis(timeStampInSec);
    return DateFormat.format("dd MMM yyyy hh:mm a", cal.getTime())
        .toString();
  }

  private String getDate(long timeStampInSec) {
    Calendar cal = Calendar.getInstance(Locale.getDefault());
    cal.setTimeInMillis(timeStampInSec);
    return DateFormat.format("MMM, dd yyyy", cal.getTime())
        .toString();
  }

  private void onMoreClicked(View view, Operation operation) {
    viewModel.showMoreDetails(view.getContext(), operation);
  }

  private void setUiContent(long timeStamp, String value, String symbol, String paidAmount,
      String paidCurrency, String localFiatAmount, String localFiatCurrency, String icon, String id,
      String description, int typeStr, int typeIcon, int statusStr, int statusColor, String to,
      boolean isSent, boolean isRevertTransaction, boolean isRevertedTransaction,
      int revertedDescription, int descriptionColor) {
    ((TextView) findViewById(R.id.transaction_timestamp)).setText(getDateAndTime(timeStamp));
    findViewById(R.id.transaction_timestamp).setVisibility(View.VISIBLE);

    formatValues(value, symbol, paidAmount, paidCurrency, localFiatAmount, localFiatCurrency,
        transaction.getType(), isRevertTransaction, isRevertedTransaction);

    ImageView typeIconImageView = findViewById(R.id.img);
    if (icon != null) {
      String path;
      if (icon.startsWith("http://") || icon.startsWith("https://")) {
        path = icon;
      } else {
        path = "file:" + icon;
      }

      GlideApp.with(this)
          .load(path)
          .apply(RequestOptions.bitmapTransform(new CircleCrop()))
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(typeIconImageView);
    } else {
      if (typeIcon != -1) {
        typeIconImageView.setImageResource(typeIcon);
        typeIconImageView.setVisibility(View.VISIBLE);
      } else {
        typeIconImageView.setVisibility(View.GONE);
      }
    }

    ((TextView) findViewById(R.id.app_id)).setText(id);
    if (description != null) {
      ((TextView) findViewById(R.id.item_id)).setText(description);
      findViewById(R.id.item_id).setVisibility(View.VISIBLE);
    }
    ((TextView) findViewById(R.id.category_name)).setText(typeStr);

    if (typeIcon != -1) {
      ((ImageView) findViewById(R.id.category_icon)).setImageResource(typeIcon);
      findViewById(R.id.category_icon).setVisibility(View.VISIBLE);
      findViewById(R.id.category_icon_background).setVisibility(View.VISIBLE);
    } else {
      findViewById(R.id.category_icon_background).setVisibility(View.GONE);
    }

    ((TextView) findViewById(R.id.status)).setText(statusStr);
    ((TextView) findViewById(R.id.status)).setTextColor(getResources().getColor(statusColor));

    if (to != null) {
      ((TextView) findViewById(R.id.to)).setText(
          isSent ? getString(R.string.label_to) : getString(R.string.label_from));
      findViewById(R.id.to_label).setVisibility(View.VISIBLE);
      ((TextView) findViewById(R.id.to)).setText(to);
      findViewById(R.id.to).setVisibility(View.VISIBLE);
      detailsList.setVisibility(View.GONE);
      findViewById(R.id.details_label).setVisibility(View.GONE);
    }

    if (isRevertTransaction || isRevertedTransaction) {
      findViewById(R.id.details_label).setVisibility(View.GONE);
      TextView tvRevertedDescription = findViewById(R.id.reverted_description);
      if (revertedDescription != -1) {
        tvRevertedDescription.setText(revertedDescription);
        tvRevertedDescription.setTextColor(getResources().getColor(descriptionColor));
        tvRevertedDescription.setVisibility(View.VISIBLE);
      } else {
        tvRevertedDescription.setVisibility(View.GONE);
      }

      if (!transaction.getLinkedTx()
          .isEmpty()) {
        Transaction link = transaction.getLinkedTx()
            .get(0);
        setupRevertedUi(link, isRevertTransaction, isRevertedTransaction);
      }
    }
  }

  private void setupRevertedUi(Transaction linkTransaction, boolean isRevertTransaction,
      boolean isRevertedTransaction) {

    View revertView = findViewById(R.id.layout_revert_transaction);
    View revertedView = findViewById(R.id.layout_reverted_transaction);
    if (isRevertTransaction) {
      revertView.setVisibility(View.GONE);
      revertedView.setVisibility(View.VISIBLE);

      ImageView logo = revertedView.findViewById(R.id.layout_support_logo);
      ImageView icn = revertedView.findViewById(R.id.layout_support_icn);

      logo.setOnClickListener(view -> viewModel.showSupportScreen());
      icn.setOnClickListener(view -> viewModel.showSupportScreen());

      revertedView.setOnClickListener(
          view -> viewModel.showDetails(view.getContext(), linkTransaction, globalBalanceCurrency));

      NetworkInfo networkInfo = viewModel.transactionsDetailsModel()
          .getValue()
          .getNetworkInfo();
      String symbol =
          linkTransaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
              : linkTransaction.getCurrency();

      String icon = null;
      TransactionDetails details = linkTransaction.getDetails();

      if (details != null) {
        icon = details.getIcon()
            .getUri();
      }

      @DrawableRes int typeIcon = R.drawable.ic_transaction_peer;

      View button = revertedView.findViewById(R.id.reverted_more_detail);
      TextView address = revertedView.findViewById(R.id.address);

      switch (linkTransaction.getType()) {
        case IAP_OFFCHAIN:
          button.setVisibility(View.VISIBLE);
          typeIcon = R.drawable.ic_transaction_iab;
          button.setOnClickListener(
              view -> viewModel.showMoreDetailsBds(view.getContext(), linkTransaction));
          address.setText(
              details.getSourceName() == null ? linkTransaction.getTo() : details.getSourceName());
          break;
        case BONUS:
          button.setVisibility(View.VISIBLE);
          typeIcon = -1;
          button.setOnClickListener(
              view -> viewModel.showMoreDetailsBds(view.getContext(), linkTransaction));
          symbol = getString(R.string.p2p_send_currency_appc_c);
          address.setText(R.string.transaction_type_bonus);
          break;
        case TOP_UP:
          typeIcon = R.drawable.transaction_type_top_up;
          button.setVisibility(View.VISIBLE);
          button.setOnClickListener(
              view -> viewModel.showMoreDetailsBds(view.getContext(), linkTransaction));
          symbol = getString(R.string.p2p_send_currency_appc_c);
          address.setText(R.string.topup_home_button);
          break;
      }
      String sourceDescription = details.getDescription() == null ? "" : details.getDescription();

      setupRevertedUi(icon, typeIcon, getValue(linkTransaction, symbol), symbol,
          getDate(linkTransaction.getTimeStamp()), sourceDescription);
    } else if (isRevertedTransaction) {
      revertView.setVisibility(View.VISIBLE);
      revertedView.setVisibility(View.GONE);
      TextView body = revertView.findViewById(R.id.message);

      ImageView logo = revertView.findViewById(R.id.layout_support_logo);
      ImageView icn = revertView.findViewById(R.id.layout_support_icn);

      logo.setOnClickListener(view -> viewModel.showSupportScreen());
      icn.setOnClickListener(view -> viewModel.showSupportScreen());

      String date = getDate(linkTransaction.getTimeStamp());
      switch (linkTransaction.getType()) {
        case TOP_UP_REVERT:
          body.setText(getString(R.string.transaction_type_reverted_topup_body, date));
          break;
        case IAP_REVERT:
          body.setText(getString(R.string.transaction_type_reverted_purchase_body, date));
          break;
        case BONUS_REVERT:
          body.setText(getString(R.string.transaction_type_reverted_bonus_body, date));
          break;
      }
    }
  }

  private void setupRevertedUi(String icon, int typeIcon, String value, String symbol, String date,
      String sourceDescription) {
    View revertedView = findViewById(R.id.layout_reverted_transaction);

    TextView originalDate = revertedView.findViewById(R.id.original_date);
    TextView valueTv = revertedView.findViewById(R.id.value);
    ImageView typeIconImageView = revertedView.findViewById(R.id.img);
    TextView sourceDescriptionTv = revertedView.findViewById(R.id.description);

    originalDate.setText(date);
    originalDate.setVisibility(View.VISIBLE);

    sourceDescriptionTv.setText(sourceDescription);

    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.color_grey_9e);
    valueTv.setText(BalanceUtils.formatBalance(value, symbol, smallTitleSize, color));

    if (icon != null) {
      String path;
      if (icon.startsWith("http://") || icon.startsWith("https://")) {
        path = icon;
      } else {
        path = "file:" + icon;
      }

      GlideApp.with(this)
          .load(path)
          .apply(RequestOptions.bitmapTransform(new CircleCrop()))
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(typeIconImageView);
    } else {
      if (typeIcon != -1) {
        typeIconImageView.setImageResource(typeIcon);
        typeIconImageView.setVisibility(View.VISIBLE);
      } else {
        typeIconImageView.setVisibility(View.GONE);
      }
    }
  }

  private void hideDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
      dialog = null;
    }
  }

  private void formatValues(String value, String symbol, String paidAmount, String paidCurrency,
      String convertedAmount, String convertedCurrency, Transaction.TransactionType type,
      boolean isRevert, boolean isReverted) {
    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.color_grey_9e);
    String signal = "";
    if (isRevert && type != Transaction.TransactionType.IAP_REVERT) {
      signal = "-";
    } else if (isReverted && type != Transaction.TransactionType.IAP_OFFCHAIN) {
      signal = "+";
    } else if (!isReverted && !isRevert) {
      signal = isSent ? "-" : "+";
    }

    if (paidAmount != null) {
      String formattedValue = signal + value + " " + symbol.toUpperCase();
      String formattedPaidValue = signal + paidAmount;
      String formattedLocalFiatValue = signal + convertedAmount + " " + convertedCurrency;
      this.paidAmount.setText(
          BalanceUtils.formatBalance(formattedPaidValue, paidCurrency, smallTitleSize, color));
      this.amount.setText(formattedValue);

      handleLocalFiatVisibility(paidCurrency, formattedLocalFiatValue, convertedCurrency);
    } else {
      String formattedValue = signal + value;
      this.paidAmount.setText(
          BalanceUtils.formatBalance(formattedValue, symbol, smallTitleSize, color));
    }
  }

  private void handleLocalFiatVisibility(String paidCurrency, String formattedConvertedAmount,
      String convertedCurrency) {
    if (convertedCurrency.equals(paidCurrency)) {
      this.localFiatAmount.setVisibility(View.GONE);
      this.verticalSeparator.setVisibility(View.GONE);
    } else {
      this.localFiatAmount.setVisibility(View.VISIBLE);
      this.verticalSeparator.setVisibility(View.VISIBLE);
      this.localFiatAmount.setText(formattedConvertedAmount);
    }
  }

  private String getValue(String currencySymbol) {
    String rawValue = transaction.getValue();
    if (!rawValue.equals("0")) {
      rawValue = getScaledValue(rawValue, DECIMALS, currencySymbol);
    }
    return rawValue;
  }

  private String getValue(Transaction linkedTx, String currencySymbol) {
    String rawValue = linkedTx.getValue();
    if (!rawValue.equals("0")) {
      rawValue = getScaledValue(rawValue, DECIMALS, currencySymbol);
    }
    if (linkedTx.getType() == Transaction.TransactionType.IAP_OFFCHAIN) {
      rawValue = "-" + rawValue;
    } else {
      rawValue = "+" + rawValue;
    }
    return rawValue;
  }

  private String getPaidValue() {
    String rawValue = transaction.getPaidAmount();
    if (rawValue != null && !rawValue.equals("0")) {
      rawValue = getScaledValue(rawValue, 0, "");
    }
    return rawValue;
  }

  private boolean isRevertTransaction(Transaction transaction) {
    return transaction.getType() == Transaction.TransactionType.BONUS_REVERT
        || transaction.getType() == Transaction.TransactionType.IAP_REVERT
        || transaction.getType() == Transaction.TransactionType.TOP_UP_REVERT;
  }

  private boolean isRevertedTransaction(Transaction transaction) {
    return !isRevertTransaction(transaction)
        && transaction.getLinkedTx()
        .size() > 0;
  }
}
