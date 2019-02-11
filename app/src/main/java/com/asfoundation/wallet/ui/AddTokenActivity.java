package com.asfoundation.wallet.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.Address;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.viewmodel.AddTokenViewModel;
import com.asfoundation.wallet.viewmodel.AddTokenViewModelFactory;
import com.asfoundation.wallet.widget.SystemView;
import com.google.android.material.textfield.TextInputLayout;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

public class AddTokenActivity extends BaseActivity implements View.OnClickListener {

  @Inject protected AddTokenViewModelFactory addTokenViewModelFactory;
  private AddTokenViewModel viewModel;

  private TextInputLayout addressLayout;
  private TextView address;
  private TextInputLayout symbolLayout;
  private TextView symbol;
  private TextInputLayout decimalsLayout;
  private TextView decimals;
  private SystemView systemView;
  private Dialog dialog;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_add_token);

    toolbar();

    addressLayout = findViewById(R.id.address_input_layout);
    address = findViewById(R.id.address);
    symbolLayout = findViewById(R.id.symbol_input_layout);
    symbol = findViewById(R.id.symbol);
    decimalsLayout = findViewById(R.id.decimal_input_layout);
    decimals = findViewById(R.id.decimals);
    systemView = findViewById(R.id.system_view);
    systemView.hide();

    findViewById(R.id.save).setOnClickListener(this);

    viewModel = ViewModelProviders.of(this, addTokenViewModelFactory)
        .get(AddTokenViewModel.class);
    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.result()
        .observe(this, this::onSaved);
  }

  private void onSaved(boolean result) {
    if (result) {
      viewModel.showTokens(this);
      finish();
    }
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    dialog = new AlertDialog.Builder(this).setTitle(R.string.title_dialog_error)
        .setMessage(R.string.error_add_token)
        .setPositiveButton(R.string.try_again, null)
        .create();
    dialog.show();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.save: {
        onSave();
      }
      break;
    }
  }

  private void onSave() {
    boolean isValid = true;
    String address = this.address.getText()
        .toString()
        .toLowerCase();
    String symbol = this.symbol.getText()
        .toString()
        .toLowerCase();
    String rawDecimals = this.decimals.getText()
        .toString();
    int decimals = 0;

    if (TextUtils.isEmpty(address)) {
      addressLayout.setError(getString(R.string.error_field_required));
      isValid = false;
    }

    if (TextUtils.isEmpty(symbol)) {
      symbolLayout.setError(getString(R.string.error_field_required));
      isValid = false;
    }

    if (TextUtils.isEmpty(rawDecimals)) {
      decimalsLayout.setError(getString(R.string.error_field_required));
      isValid = false;
    }

    try {
      decimals = Integer.valueOf(rawDecimals);
    } catch (NumberFormatException ex) {
      decimalsLayout.setError(getString(R.string.error_must_numeric));
      isValid = false;
    }

    if (!Address.isAddress(address)) {
      addressLayout.setError(getString(R.string.error_invalid_address));
      isValid = false;
    }

    if (isValid) {
      viewModel.save(address, symbol, decimals);
    }
  }
}
