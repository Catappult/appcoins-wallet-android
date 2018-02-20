package com.asf.wallet.ui;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.asf.wallet.C;
import com.asf.wallet.R;
import com.asf.wallet.entity.ErrorEnvelope;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.util.BalanceUtils;
import com.asf.wallet.viewmodel.ConfirmationViewModel;
import com.asf.wallet.viewmodel.ConfirmationViewModelFactory;
import com.asf.wallet.viewmodel.GasSettingsViewModel;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;
import javax.inject.Inject;

import static com.asf.wallet.C.EXTRA_GAS_SETTINGS;
import static com.asf.wallet.C.EXTRA_TRANSACTION_BUILDER;
import static com.asf.wallet.C.GWEI_UNIT;

public class ConfirmationActivity extends BaseActivity {
  AlertDialog dialog;

  @Inject ConfirmationViewModelFactory confirmationViewModelFactory;
  ConfirmationViewModel viewModel;

  private TextView fromAddressText;
  private TextView toAddressText;
  private TextView valueText;
  private TextView gasPriceText;
  private TextView gasLimitText;
  private TextView networkFeeText;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_confirm);
    toolbar();

    fromAddressText = findViewById(R.id.text_from);
    toAddressText = findViewById(R.id.text_to);
    valueText = findViewById(R.id.text_value);
    gasPriceText = findViewById(R.id.text_gas_price);
    gasLimitText = findViewById(R.id.text_gas_limit);
    networkFeeText = findViewById(R.id.text_network_fee);
    findViewById(R.id.send_button).setOnClickListener(view -> onSend());

    viewModel = ViewModelProviders.of(this, confirmationViewModelFactory)
        .get(ConfirmationViewModel.class);
    viewModel.transactionBuilder()
        .observe(this, this::onTransactionBuilder);
    viewModel.transactionHash()
        .observe(this, this::onTransaction);

    viewModel.progress()
        .observe(this, this::onProgress);
    viewModel.error()
        .observe(this, this::onError);
  }

  private void onTransactionBuilder(TransactionBuilder transactionBuilder) {
    fromAddressText.setText(transactionBuilder.fromAddress());
    toAddressText.setText(transactionBuilder.toAddress());

    valueText.setText(getString(R.string.new_transaction_value, transactionBuilder.amount(),
        transactionBuilder.symbol()));
    valueText.setTextColor(ContextCompat.getColor(this, R.color.red));
    BigDecimal gasPrice = transactionBuilder.gasSettings().gasPrice;
    BigDecimal gasLimit = transactionBuilder.gasSettings().gasLimit;
    gasPriceText.setText(
        getString(R.string.gas_price_value, BalanceUtils.weiToGwei(gasPrice), GWEI_UNIT));
    gasLimitText.setText(transactionBuilder.gasSettings().gasLimit.toPlainString());

    String networkFee = BalanceUtils.weiToEth(gasPrice.multiply(gasLimit))
        .toPlainString() + " " + C.ETH_SYMBOL;
    networkFeeText.setText(networkFee);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.confirmation_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_edit: {
        viewModel.openGasSettings(ConfirmationActivity.this);
      }
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onProgress(boolean shouldShowProgress) {
    hideDialog();
    if (shouldShowProgress) {
      dialog = new AlertDialog.Builder(this).setTitle(R.string.title_dialog_sending)
          .setView(new ProgressBar(this))
          .setCancelable(false)
          .create();
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

  private void onTransaction(String hash) {
    hideDialog();
    dialog = new AlertDialog.Builder(this).setTitle(R.string.transaction_succeeded)
        .setMessage(hash)
        .setPositiveButton(R.string.button_ok, (dialog1, id) -> {
          successFinish(hash);
        })
        .setNeutralButton(R.string.copy, (dialog1, id) -> {
          ClipboardManager clipboard =
              (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clip = ClipData.newPlainText("transaction hash", hash);
          clipboard.setPrimaryClip(clip);
          successFinish(hash);
        })
        .create();
    dialog.show();
  }

  private void successFinish(String hash) {
    Intent intent = new Intent();
    intent.putExtra("transaction_hash", hash);
    setResult(Activity.RESULT_OK, intent);
    finish();
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

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == GasSettingsViewModel.SET_GAS_SETTINGS) {
      if (resultCode == RESULT_OK) {
        viewModel.setGasSettings(intent.getParcelableExtra(EXTRA_GAS_SETTINGS));
      }
    }
  }

  @Override protected void onResume() {
    super.onResume();

    viewModel.init(getIntent().getParcelableExtra(EXTRA_TRANSACTION_BUILDER));
  }
}
