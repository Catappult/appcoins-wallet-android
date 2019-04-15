package com.asfoundation.wallet.ui;

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
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.transactions.TransactionDetails;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsDetailsAdapter;
import com.asfoundation.wallet.util.BalanceUtils;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModel;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import com.asfoundation.wallet.widget.CircleTransformation;
import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.CompositeDisposable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailActivity extends BaseActivity {

  @Inject TransactionDetailViewModelFactory transactionDetailViewModelFactory;
  private TransactionDetailViewModel viewModel;

  private Transaction transaction;
  private boolean isSent = false;
  private TextView amount;
  private TransactionsDetailsAdapter adapter;
  private RecyclerView detailsList;

  private Dialog dialog;
  private CompositeDisposable disposables;

  private static final int DECIMALS = 18;

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

    viewModel = ViewModelProviders.of(this, transactionDetailViewModelFactory)
        .get(TransactionDetailViewModel.class);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);

    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          float percentage =
              1 - ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
          findViewById(R.id.src_img).setScaleX(percentage);
          findViewById(R.id.src_img).setScaleY(percentage);
        });
  }

  @Override protected void onStop() {
    super.onStop();
    disposables.dispose();
    hideDialog();
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);

    if (!transaction.getOperations()
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
    View categorybackground = findViewById(R.id.category_icon_background);

    switch (transaction.getType()) {
      case ADS:
        typeStr = R.string.transaction_type_poa;
        typeIcon = R.drawable.ic_transaction_poa;
        break;
      case ADS_OFFCHAIN:
        typeStr = R.string.transaction_type_poa_offchain;
        typeIcon = R.drawable.ic_transaction_poa;
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case IAP_OFFCHAIN:
        button.setVisibility(View.VISIBLE);
        to = transaction.getTo();
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
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
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case TOP_UP:
        typeStr = R.string.topup_title;
        id = getString(R.string.topup_title);
        categorybackground.setBackground(null);
        typeIcon = R.drawable.transaction_type_top_up;
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        symbol = getString(R.string.p2p_send_currency_appc_c);
        break;
      case TRANSFER_OFF_CHAIN:
        typeStr = R.string.transaction_type_p2p;
        id = isSent ? "Transfer Sent" : getString(R.string.askafriend_received_title);
        typeIcon = R.drawable.transaction_type_transfer_off_chain;
        categorybackground.setBackground(null);
        to = transaction.getTo();
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(
            view -> viewModel.showMoreDetailsBds(view.getContext(), transaction));
        symbol = getString(R.string.p2p_send_currency_appc_c);
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

    setUIContent(transaction.getTimeStamp(), getValue(), symbol, icon, id, description, typeStr,
        typeIcon, statusStr, statusColor, to, isSent);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);

    String symbol =
        transaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
            : transaction.getCurrency();
    formatValue(getValue(), symbol);
  }

  private String getScaledValue(String valueStr) {
    // Perform decimal conversion
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, DECIMALS)));
    int scale = 3 - value.precision() + value.scale();
    return value.setScale(scale, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
  }

  private String getDate(long timeStampInSec) {
    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    cal.setTimeInMillis(timeStampInSec * 1000);
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

      Picasso.with(this)
          .load(path)
          .transform(new CircleTransformation())
          .fit()
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

    amount.setText(BalanceUtils.formatBalance(value, symbol, smallTitleSize, color));
  }

  private String getValue() {
    String rawValue = transaction.getValue();
    if (!rawValue.equals("0")) {
      rawValue = (isSent ? "-" : "+") + getScaledValue(rawValue);
    }
    return rawValue;
  }
}
