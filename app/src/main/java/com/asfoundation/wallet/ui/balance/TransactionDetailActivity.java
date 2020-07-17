package com.asfoundation.wallet.ui.balance;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
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
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.subscriptions.ActiveSubscriptionDetails;
import com.asfoundation.wallet.subscriptions.SubscriptionDetails;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.BaseActivity;
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

import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailActivity extends BaseActivity {

  private static final int DECIMALS = 18;
  @Inject TransactionDetailViewModelFactory transactionDetailViewModelFactory;
  @Inject CurrencyFormatUtils formatter;
  private TransactionDetailViewModel viewModel;
  private Transaction transaction;
  private boolean isSent = false;
  private TextView amount;
  private TransactionsDetailsAdapter adapter;
  private RecyclerView detailsList;
  private Dialog dialog;
  private CompositeDisposable disposables;
  private View paymentMethodLabel;
  private View paymentMethod;
  private Button cancelSubscription;
  private View cancelSubscriptionLayout;
  private View logo;
  private View brand;

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

    ((ToolbarArcBackground) findViewById(R.id.toolbar_background_arc)).setScale(1f);

    amount = findViewById(R.id.amount);
    adapter = new TransactionsDetailsAdapter(this::onMoreClicked);
    detailsList = findViewById(R.id.details_list);
    detailsList.setAdapter(adapter);

    paymentMethodLabel = findViewById(R.id.payment_method_label);
    paymentMethod = findViewById(R.id.payment_method);
    cancelSubscriptionLayout = findViewById(R.id.cancel__subscription_layout);
    cancelSubscription = findViewById(R.id.cancel_subscription_button);
    logo = findViewById(R.id.logo);
    brand = findViewById(R.id.brand);

    viewModel = ViewModelProviders.of(this, transactionDetailViewModelFactory)
        .get(TransactionDetailViewModel.class);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
    viewModel.subscriptionDetails()
        .observe(this, this::onSubscriptionDetails);

    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          float percentage =
              1 - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
          findViewById(R.id.img).setScaleX(percentage);
          findViewById(R.id.img).setScaleY(percentage);
        });
  }

  @Override protected void onStop() {
    super.onStop();
    disposables.dispose();
    hideDialog();
  }

  private void onSubscriptionDetails(SubscriptionDetails subscriptionDetails) {
    paymentMethodLabel.setVisibility(View.VISIBLE);
    paymentMethod.setVisibility(View.VISIBLE);
    logo.setVisibility(View.GONE);
    brand.setVisibility(View.GONE);
    GlideApp.with(this)
        .load(subscriptionDetails.getPaymentMethodUrl())
        .into((ImageView) findViewById(R.id.payment_method_icon));

    ((TextView) findViewById(R.id.payment_method_value)).setText(
        subscriptionDetails.getPaymentMethod());

    boolean isActiveSubscription = subscriptionDetails instanceof ActiveSubscriptionDetails;

    if (cancelSubscription != null && cancelSubscriptionLayout != null && isActiveSubscription) {
      cancelSubscriptionLayout.setVisibility(View.VISIBLE);
      cancelSubscription.setOnClickListener(view -> viewModel.cancelSubscription(view.getContext(),
          subscriptionDetails.getPackageName()));
    }
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);

    if (transaction.getOperations() != null && !transaction.getOperations()
        .isEmpty()) {
      adapter.addOperations(transaction.getOperations());
      detailsList.setVisibility(View.VISIBLE);
    }

    isSent = transaction.getFrom()
        .toLowerCase()
        .equals(wallet.address);

    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();

    String symbol =
        transaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
            : transaction.getCurrency();

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

    @StringRes int typeStr = R.string.transaction_type_standard;
    @DrawableRes int typeIcon = R.drawable.ic_transaction_peer;
    View button = findViewById(R.id.more_detail);
    View manageSubscriptions = findViewById(R.id.manage_subscriptions);
    View categoryBackground = findViewById(R.id.category_icon_background);

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
      case IAP_OFFCHAIN:
        button.setVisibility(View.VISIBLE);
        to = transaction.getTo();
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        manageSubscriptions.setVisibility(View.GONE);
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
      case SUBS:
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
        viewModel.loadSubscriptionDetails(transaction.getTransactionId());
        break;
    }

    @StringRes int statusStr = R.string.transaction_status_success;
    @ColorRes int statusColor = R.color.green;

    switch (transaction.getStatus()) {
      case FAILED:
        statusStr = R.string.transaction_status_failed;
        statusColor = R.color.red;
        break;
      case PENDING:
        statusStr = R.string.transaction_status_pending;
        statusColor = R.color.orange;
        break;
    }

    setUIContent(transaction.getTimeStamp(), getValue(symbol), symbol, icon, id, description,
        typeStr, typeIcon, statusStr, statusColor, to, isSent);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);

    String symbol =
        transaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
            : transaction.getCurrency();
    formatValue(getValue(symbol), symbol);
  }

  private String getScaledValue(String valueStr, String currencySymbol) {
    WalletCurrency walletCurrency = WalletCurrency.mapToWalletCurrency(currencySymbol);
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, DECIMALS)));
    return formatter.formatCurrency(value, walletCurrency);
  }

  private String getDate(long timeStampInSec) {
    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    cal.setTimeInMillis(timeStampInSec);
    return DateFormat.format("dd MMM yyyy hh:mm a", cal.getTime())
        .toString();
  }

  private void onMoreClicked(View view, Operation operation) {
    viewModel.showMoreDetails(view.getContext(), operation);
  }

  private void setUIContent(long timeStamp, String value, String symbol, String icon, String id,
      String description, int typeStr, int typeIcon, int statusStr, int statusColor, String to,
      boolean isSent) {
    ((TextView) findViewById(R.id.transaction_timestamp)).setText(getDate(timeStamp));
    findViewById(R.id.transaction_timestamp).setVisibility(View.VISIBLE);

    formatValue(value, symbol);

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
  }

  private void hideDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
      dialog = null;
    }
  }

  private void formatValue(String value, String symbol) {
    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.color_grey_9e);
    String formattedValue = (isSent ? "-" : "+") + value;
    amount.setText(BalanceUtils.formatBalance(formattedValue, symbol, smallTitleSize, color));
  }

  private String getValue(String currencySymbol) {
    String rawValue = transaction.getValue();
    if (!rawValue.equals("0")) {
      rawValue = getScaledValue(rawValue, currencySymbol);
    }
    return rawValue;
  }
}
