package com.asfoundation.wallet.ui;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsDetailsAdapter;
import com.asfoundation.wallet.util.BalanceUtils;
import com.asfoundation.wallet.util.StringUtils;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModel;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import com.asfoundation.wallet.widget.CircleTransformation;
import com.squareup.picasso.Picasso;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Locale;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.Key.TRANSACTION;

public class TransactionDetailActivity extends BaseActivity {

  private static final String TAG = TransactionDetailActivity.class.getSimpleName();
  @Inject TransactionDetailViewModelFactory transactionDetailViewModelFactory;
  private TransactionDetailViewModel viewModel;

  private Transaction transaction;
  private TextView amount;
  private TransactionsDetailsAdapter adapter;
  private RecyclerView detailsList;

  private Dialog dialog;
  private String walletAddr;
  private CompositeDisposable disposables;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AndroidInjection.inject(this);

    setContentView(R.layout.activity_transaction_detail);
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
  }

  @Override protected void onStop() {
    super.onStop();
    disposables.dispose();
    hideDialog();
  }

  private void onDefaultWallet(Wallet wallet) {
    walletAddr = wallet.address;
    adapter.setDefaultWallet(wallet);
    adapter.addOperations(transaction.getOperations());

    boolean isSent = transaction.getFrom()
        .toLowerCase()
        .equals(wallet.address);

    long decimals = 18;
    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();

    String rawValue = transaction.getValue();
    if (!rawValue.equals("0")) {
      rawValue = (isSent ? "-" : "+") + getScaledValue(rawValue, decimals);
    }

    String symbol =
        transaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
            : transaction.getCurrency();

    String icon = null;
    String id = transaction.getTransactionId();
    String description = null;
    if (transaction.getDetails() != null) {
      icon = transaction.getDetails()
          .getIcon();
      id = transaction.getDetails()
          .getSourceName();
      description = transaction.getDetails()
          .getDescription();
    }
    String to = null;

    @StringRes int typeStr = R.string.transaction_type_standard;
    @DrawableRes int typeIcon = R.drawable.ic_transaction_peer;
    boolean showCloseButton = false;

    switch (transaction.getType()) {
      case ADS:
        typeStr = R.string.transaction_type_poa;
        typeIcon = R.drawable.ic_transaction_poa;
        break;
      case MICRO_IAB:
        to = transaction.getTo();
      case IAB:
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
        break;
      case OPEN_CHANNEL:
        typeStr = R.string.transaction_type_miuraiden;
        typeIcon = R.drawable.ic_transaction_miu;
        id = getString(R.string.miuraiden_trans_details_open);
        description =
            StringUtils.resizeString(isSent ? transaction.getTo() : transaction.getFrom(), 7,
                getString(R.string.ellipsize));
        break;
      case TOP_UP_CHANNEL:
        typeStr = R.string.transaction_type_miuraiden;
        typeIcon = R.drawable.ic_transaction_miu;
        id = getString(R.string.miuraiden_trans_details_topup);
        description =
            StringUtils.resizeString(isSent ? transaction.getTo() : transaction.getFrom(), 7,
                getString(R.string.ellipsize));
        break;
      case CLOSE_CHANNEL:
        typeStr = R.string.transaction_type_miuraiden;
        typeIcon = R.drawable.ic_transaction_miu;
        id = getString(R.string.miuraiden_trans_details_close);
        description =
            StringUtils.resizeString(isSent ? transaction.getTo() : transaction.getFrom(), 7,
                getString(R.string.ellipsize));
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

    setUIContent(transaction.getTimeStamp(), rawValue, symbol, icon, id, description, typeStr,
        typeIcon, statusStr, statusColor, showCloseButton, to);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);
  }

  private String getScaledValue(String valueStr, long decimals) {
    // Perform decimal conversion
    BigDecimal value = new BigDecimal(valueStr);
    value = value.divide(new BigDecimal(Math.pow(10, decimals)));
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
      String description, int typeStr, int typeIcon, int statusStr, int statusColor,
      boolean showCloseBtn, String to) {
    ((TextView) findViewById(R.id.transaction_timestamp)).setText(getDate(timeStamp));
    findViewById(R.id.transaction_timestamp).setVisibility(View.VISIBLE);

    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.gray_alpha_8a);

    amount.setText(BalanceUtils.formatBalance(value, symbol, smallTitleSize, color));

    if (icon != null) {
      Picasso.with(this)
          .load("file:" + icon)
          .transform(new CircleTransformation())
          .fit()
          .into((ImageView) findViewById(R.id.img));
    } else {
      ((ImageView) findViewById(R.id.img)).setImageResource(typeIcon);
    }

    ((TextView) findViewById(R.id.app_id)).setText(id);
    if (description != null) {
      ((TextView) findViewById(R.id.item_id)).setText(description);
      findViewById(R.id.item_id).setVisibility(View.VISIBLE);
    }
    ((TextView) findViewById(R.id.category_name)).setText(typeStr);
    ((ImageView) findViewById(R.id.category_icon)).setImageResource(typeIcon);

    ((TextView) findViewById(R.id.status)).setText(statusStr);
    ((TextView) findViewById(R.id.status)).setTextColor(getResources().getColor(statusColor));

    if (to != null) {
      ((TextView) findViewById(R.id.to)).setText(to);
      detailsList.setVisibility( View.GONE);
      findViewById(R.id.details_label).setVisibility(View.GONE);
      findViewById(R.id.to_label).setVisibility(View.VISIBLE);
      findViewById(R.id.to).setVisibility(View.VISIBLE);
    }

    findViewById(R.id.close_channel_btn).setVisibility(showCloseBtn ? View.VISIBLE : View.GONE);
    findViewById(R.id.close_channel_btn).setOnClickListener(v -> showCloseChannelConfirmation());
  }

  private void showCloseChannelConfirmation() {
    AlertDialog.Builder builder = buildDialog();
    View view = getLayoutInflater().inflate(R.layout.dialog_close_channel, null);
    view.findViewById(R.id.positive_button)
        .setOnClickListener(v -> {
          showLoading();
          disposables.add(Completable.complete()
              .observeOn(Schedulers.io())
              .andThen(viewModel.closeChannel(walletAddr))
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(this::hideDialog, throwable -> {
                Log.e(TAG, "Failed to close channel.", throwable);
                hideDialog();
              }));
        });
    view.findViewById(R.id.negative_button)
        .setOnClickListener(v -> hideDialog());

    builder.setView(view);
    dialog = builder.create();
    dialog.show();
  }

  private void showLoading() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
    builder.setView(view);
    builder.setCancelable(false);
    hideDialog();
    dialog = builder.create();
    dialog.getWindow()
        .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.show();
  }

  private AlertDialog.Builder buildDialog() {
    hideDialog();
    return new AlertDialog.Builder(this);
  }

  private void hideDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
      dialog = null;
    }
  }
}
