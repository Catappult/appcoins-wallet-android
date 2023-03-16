package com.asfoundation.wallet.transfers;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import com.appcoins.wallet.core.utils.common.BalanceUtils;
import com.asf.wallet.R;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.PendingTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.ui.BaseActivity;
import com.appcoins.wallet.ui.widgets.WalletButtonView;
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils;
import com.appcoins.wallet.core.utils.common.Log;
import com.appcoins.wallet.core.utils.common.WalletCurrency;
import com.asfoundation.wallet.viewmodel.GasSettingsViewModel;
import com.asfoundation.wallet.viewmodel.TransferConfirmationViewModel;
import com.asfoundation.wallet.viewmodel.TransferConfirmationViewModelFactory;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.EXTRA_GAS_SETTINGS;
import static com.asfoundation.wallet.C.EXTRA_TRANSACTION_BUILDER;
import static com.asfoundation.wallet.C.GWEI_UNIT;

@AndroidEntryPoint public class TransferConfirmationActivity extends BaseActivity {
  private static final String TAG = TransferConfirmationActivity.class.getSimpleName();

  AlertDialog dialog;
  CurrencyFormatUtils currencyFormatUtils;
  @Inject TransferConfirmationViewModelFactory viewModelFactory;
  private TransferConfirmationViewModel viewModel;
  private TextView fromAddressText;
  private TextView toAddressText;
  private TextView valueText;
  private TextView gasPriceText;
  private TextView gasLimitText;
  private TextView networkFeeText;
  private WalletButtonView sendButton;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_confirm);
    toolbar();
    currencyFormatUtils = new CurrencyFormatUtils();
    fromAddressText = findViewById(R.id.text_from);
    toAddressText = findViewById(R.id.text_to);
    valueText = findViewById(R.id.text_value);
    gasPriceText = findViewById(R.id.text_gas_price);
    gasLimitText = findViewById(R.id.text_gas_limit);
    networkFeeText = findViewById(R.id.text_network_fee);
    sendButton = findViewById(R.id.send_button);
    sendButton.setOnClickListener(view -> onSend());

    viewModel =
        new ViewModelProvider(this, viewModelFactory).get(TransferConfirmationViewModel.class);
    viewModel.transactionBuilder()
        .observe(this, this::onTransactionBuilder);
    viewModel.transactionHash()
        .observe(this, this::onTransaction);
    viewModel.progress()
        .observe(this, this::onProgress);
    viewModel.error()
        .observe(this, this::onError);

    TransactionBuilder transactionBuilder =
        getIntent().getParcelableExtra(EXTRA_TRANSACTION_BUILDER);
    if (transactionBuilder != null) {
      viewModel.init(transactionBuilder);
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_edit) {
      viewModel.openGasSettings(TransferConfirmationActivity.this);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == GasSettingsViewModel.SET_GAS_SETTINGS) {
      if (resultCode == RESULT_OK) {
        viewModel.setGasSettings(intent.getParcelableExtra(EXTRA_GAS_SETTINGS));
      }
    }
  }

  private void onTransactionBuilder(TransactionBuilder transactionBuilder) {
    fromAddressText.setText(transactionBuilder.fromAddress());
    toAddressText.setText(transactionBuilder.toAddress());

    String value = "-" + currencyFormatUtils.formatTransferCurrency(transactionBuilder.amount(),
        WalletCurrency.ETHEREUM);
    String symbol = transactionBuilder.symbol();
    int smallTitleSize = (int) getResources().getDimension(R.dimen.small_text);
    int color = getResources().getColor(R.color.styleguide_medium_grey);
    valueText.setText(BalanceUtils.formatBalance(value, symbol, smallTitleSize, color));
    final GasSettings gasSettings =
        viewModel.handleSavedGasSettings(transactionBuilder.gasSettings().gasPrice,
            transactionBuilder.gasSettings().gasLimit);

    String formattedGasPrice = getString(R.string.gas_price_value,
        currencyFormatUtils.formatTransferCurrency(gasSettings.gasPrice, WalletCurrency.ETHEREUM),
        GWEI_UNIT);
    gasPriceText.setText(formattedGasPrice);
    gasLimitText.setText(gasSettings.gasLimit.toPlainString());

    String networkFee = currencyFormatUtils.formatTransferCurrency(BalanceUtils.weiToEth(
        BalanceUtils.gweiToWei(gasSettings.gasPrice)
            .multiply(gasSettings.gasLimit)), WalletCurrency.ETHEREUM) + " " + C.ETH_SYMBOL;
    networkFeeText.setText(networkFee);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.confirmation_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  private void onProgress(boolean shouldShowProgress) {
    if (shouldShowProgress) {
      hideDialog();
      ProgressBar progressBar = new ProgressBar(this);
      progressBar.setIndeterminateDrawable(
          ResourcesCompat.getDrawable(getResources(), R.drawable.gradient_progress, null));
      dialog = new AlertDialog.Builder(this).setView(progressBar)
          .setCancelable(false)
          .create();
      dialog.getWindow()
          .setBackgroundDrawableResource(android.R.color.transparent);
      dialog.show();
    }
  }

  private void hideDialog() {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
  }

  private void onSend() {
    viewModel.send();
  }

  private void onTransaction(PendingTransaction transaction) {
    Log.d(TAG, "onTransaction() called with: transaction = [" + transaction + "]");
    hideDialog();
    viewModel.progressFinished();
  }

  private void onError(ErrorEnvelope error) {
    hideDialog();
    AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.error_transaction_failed)
        .setMessage(error.message)
        .setPositiveButton(R.string.button_ok, (dialog1, id) -> {
          // Do nothing
        })
        .create();
    dialog.show();
  }

  @Override protected void onResume() {
    super.onResume();
    sendPageViewEvent();
  }
}
