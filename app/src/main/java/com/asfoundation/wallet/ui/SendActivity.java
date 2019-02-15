package com.asfoundation.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import com.asf.wallet.R;
import com.asfoundation.wallet.router.Result;
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity;
import com.asfoundation.wallet.ui.iab.IabActivity;
import com.asfoundation.wallet.viewmodel.SendViewModel;
import com.asfoundation.wallet.viewmodel.SendViewModelFactory;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.textfield.TextInputLayout;
import dagger.android.AndroidInjection;
import java.math.BigDecimal;
import java.text.NumberFormat;
import javax.inject.Inject;

import static android.widget.Toast.LENGTH_SHORT;
import static com.asfoundation.wallet.C.EXTRA_TRANSACTION_BUILDER;

public class SendActivity extends BaseActivity {

  private static final int BARCODE_READER_REQUEST_CODE = 1;
  @Inject SendViewModelFactory sendViewModelFactory;
  SendViewModel viewModel;
  private EditText toAddressText;
  private EditText amountText;
  private TextInputLayout toInputLayout;
  private TextInputLayout amountInputLayout;

  public static Intent newIntent(Context context, Intent previousIntent) {
    Intent intent = new Intent(context, IabActivity.class);
    intent.setData(previousIntent.getData());
    if (previousIntent.getExtras() != null) {
      intent.putExtras(previousIntent.getExtras());
    }
    return intent;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_send);
    toolbar();

    toInputLayout = findViewById(R.id.to_input_layout);
    toAddressText = findViewById(R.id.send_to_address);
    amountInputLayout = findViewById(R.id.amount_input_layout);
    amountText = findViewById(R.id.send_amount);

    viewModel = ViewModelProviders.of(this, sendViewModelFactory)
        .get(SendViewModel.class);
    viewModel.init(getIntent().getParcelableExtra(EXTRA_TRANSACTION_BUILDER),
        getIntent().getData());

    viewModel.symbol()
        .observe(this, this::onSymbol);
    viewModel.amount()
        .observe(this, this::onAmount);
    viewModel.toAddress()
        .observe(this, this::onToAddress);
    viewModel.onTransactionSucceed()
        .observe(this, this::onFinishWithResult);

    ImageButton scanBarcodeButton = findViewById(R.id.scan_barcode_button);
    scanBarcodeButton.setOnClickListener(view -> {
      Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
      startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    });
  }

  private void onFinishWithResult(Result result) {
    if (result.isSuccess()) {
      setResult(Activity.RESULT_OK, result.getData());
      finish();
    }
  }

  private void onAmount(BigDecimal bigDecimal) {
    amountText.setText(NumberFormat.getInstance()
        .format(bigDecimal));
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.send_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_next: {
        onNext();
        break;
      }
      case android.R.id.home: {
        viewModel.showTransactions(this);
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (!viewModel.onActivityResult(requestCode, resultCode, data)
        && requestCode == BARCODE_READER_REQUEST_CODE) {
      if (resultCode == CommonStatusCodes.SUCCESS) {
        if (data != null) {
          Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
          if (!viewModel.extractFromQR(barcode)) {
            Toast.makeText(this, R.string.toast_qr_code_no_address, LENGTH_SHORT)
                .show();
          }
        }
      } else {
        Log.e("SEND", String.format(getString(R.string.barcode_error_format),
            CommonStatusCodes.getStatusCodeString(resultCode)));
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void onNext() {
    // Validate input fields
    boolean hasError = false;
    final String to = toAddressText.getText()
        .toString();
    final String amount = amountText.getText()
        .toString();

    if (!viewModel.setToAddress(to)) {
      toInputLayout.setError(getString(R.string.error_invalid_address));
      hasError = true;
    }

    if (!viewModel.setAmount(amount)) {
      amountInputLayout.setError(getString(R.string.error_invalid_amount));
      hasError = true;
    }

    if (!hasError) {
      toInputLayout.setErrorEnabled(false);
      amountInputLayout.setErrorEnabled(false);

      viewModel.openConfirmation(this);
    }
  }

  private void onToAddress(String toAddress) {
    // Populate to address if it has been passed forward
    toAddressText.setText(toAddress);
  }

  private void onSymbol(String symbol) {
    if (symbol != null) {
      setTitle(String.format(getString(R.string.title_send_with_token), symbol));
      amountInputLayout.setHint(String.format(getString(R.string.hint_amount_with_token), symbol));
    }
  }

  @Override public void onBackPressed() {
    viewModel.showTransactions(this);
  }
}
