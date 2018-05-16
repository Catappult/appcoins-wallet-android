package com.asfoundation.wallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsDetailsAdapter;
import com.asfoundation.wallet.util.BalanceUtils;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModel;
import com.asfoundation.wallet.viewmodel.TransactionDetailViewModelFactory;
import dagger.android.AndroidInjection;
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
  private TextView amount;
  private TransactionsDetailsAdapter adapter;


  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AndroidInjection.inject(this);

    setContentView(R.layout.activity_transaction_detail);

    transaction = getIntent().getParcelableExtra(TRANSACTION);
    if (transaction == null) {
      finish();
      return;
    }
    toolbar();

    // get gas on each operation
    //Operation operation = transaction.getOperations()
    //    .get(0);
    //new BigDecimal(transaction.gasUsed).multiply(new BigDecimal(transaction.gasPrice));
    amount = findViewById(R.id.amount);

    adapter = new TransactionsDetailsAdapter(this::onMoreClicked);
    RecyclerView list = findViewById(R.id.details_list);
    //list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);

    viewModel = ViewModelProviders.of(this, transactionDetailViewModelFactory)
        .get(TransactionDetailViewModel.class);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);
    adapter.addOperations(transaction.getOperations());

    boolean isSent = transaction.getFrom()
        .toLowerCase()
        .equals(wallet.address);
    String rawValue;
    String symbol;
    long decimals = 18;
    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();
    rawValue = transaction.getValue();
    symbol = transaction.getCurrency() == null ? (networkInfo == null ? "" : networkInfo.symbol)
        : transaction.getCurrency();

    if (!rawValue.equals("0")) {
      rawValue = (isSent ? "-" : "+") + getScaledValue(rawValue, decimals);
    }

    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.gray_alpha_8a);
    amount.setText(BalanceUtils.formatBalance(rawValue, symbol, smallTitleSize, color));

    ((TextView) findViewById(R.id.app_id)).setText(transaction.getTransactionId());
    ((TextView) findViewById(R.id.app_id)).setVisibility(View.VISIBLE);
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
    ((TextView) findViewById(R.id.status)).setText(statusStr);
    ((TextView) findViewById(R.id.status)).setTextColor(getResources().getColor(statusColor));

    @StringRes int typeStr = R.string.transaction_type_standard;
    @DrawableRes int typeIcon = R.drawable.ic_transaction_peer;

    switch (transaction.getType()) {
      case ADS:
        typeStr = R.string.transaction_type_poa;
        typeIcon = R.drawable.ic_transaction_poa;
        break;
      case IAB:
        typeStr = R.string.transaction_type_iab;
        typeIcon = R.drawable.ic_transaction_iab;
        break;
    }
    ((TextView) findViewById(R.id.category_name)).setText(typeStr);
    ((ImageView) findViewById(R.id.category_icon)).setImageResource(typeIcon);

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
    return DateFormat.getLongDateFormat(this)
        .format(cal.getTime());
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    //findViewById(R.id.more_detail).setVisibility(
    //    TextUtils.isEmpty(networkInfo.etherscanUrl) ? View.GONE : View.VISIBLE);
  }


  private void onMoreClicked(View view, Operation operation) {
    viewModel.showMoreDetails(view.getContext(), operation);
  }

}
