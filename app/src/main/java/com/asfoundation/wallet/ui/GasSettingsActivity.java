package com.asfoundation.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.appcoins.wallet.core.utils.android_common.BalanceUtils;
import com.asf.wallet.R;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.GasSettings;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.ui.transact.GasPriceLimitsGwei;
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils;
import com.appcoins.wallet.core.utils.android_common.WalletCurrency;
import com.asfoundation.wallet.viewmodel.GasSettingsViewModel;
import com.asfoundation.wallet.viewmodel.GasSettingsViewModelFactory;
import dagger.hilt.android.AndroidEntryPoint;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.inject.Inject;

@AndroidEntryPoint public class GasSettingsActivity extends BaseActivity {

  @Inject GasSettingsViewModelFactory viewModelFactory;
  private GasSettingsViewModel viewModel;
  private CurrencyFormatUtils currencyFormatUtils;
  private TextView gasPriceText;
  private TextView gasLimitText;
  private TextView networkFeeText;
  private TextView gasPriceInfoText;
  private TextView gasLimitInfoText;
  private SeekBar gasLimitSlider;
  private SeekBar gasPriceSlider;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_gas_settings);
    toolbar();

    currencyFormatUtils = new CurrencyFormatUtils();
    gasPriceSlider = findViewById(R.id.gas_price_slider);
    gasLimitSlider = findViewById(R.id.gas_limit_slider);
    gasPriceText = findViewById(R.id.gas_price_text);
    gasLimitText = findViewById(R.id.gas_limit_text);
    networkFeeText = findViewById(R.id.text_network_fee);
    gasPriceInfoText = findViewById(R.id.gas_price_info_text);
    gasLimitInfoText = findViewById(R.id.gas_limit_info_text);

    gasPriceSlider.setPadding(0, 0, 0, 0);
    gasLimitSlider.setPadding(0, 0, 0, 0);

    viewModel = new ViewModelProvider(this, viewModelFactory)
        .get(GasSettingsViewModel.class);

    viewModel.gasPrice()
        .observe(this, this::onGasPrice);
    viewModel.gasLimit()
        .observe(this, this::onGasLimit);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.savedGasPreferences()
        .observe(this, this::onSavedGasSettings);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_save) {
      BigDecimal gasPrice = viewModel.gasPrice()
          .getValue();
      BigDecimal gasLimit = viewModel.gasLimit()
          .getValue();
      viewModel.saveChanges(gasPrice, gasLimit);
      Intent intent = new Intent();
      intent.putExtra(C.EXTRA_GAS_SETTINGS, new GasSettings(gasPrice, gasLimit));
      setResult(RESULT_OK, intent);
      finish();
    }
    return super.onOptionsItemSelected(item);
  }

  private void onSavedGasSettings(GasSettings gasSettings) {
    BigDecimal gasPrice = new BigDecimal(getIntent().getStringExtra(C.EXTRA_GAS_PRICE));
    BigDecimal gasLimit = new BigDecimal(getIntent().getStringExtra(C.EXTRA_GAS_LIMIT));
    BigDecimal gasLimitMin = BigDecimal.valueOf(C.GAS_LIMIT_MIN);
    BigDecimal gasLimitMax = BigDecimal.valueOf(C.GAS_LIMIT_MAX);
    BigDecimal gasPriceMin = BigDecimal.valueOf(C.GAS_PRICE_MIN);
    BigInteger networkFeeMax = BigInteger.valueOf(C.NETWORK_FEE_MAX);
    GasSettings savedGasPreference = viewModel.getSavedGasPreferences();
    BigDecimal savedGasPrice = savedGasPreference.gasPrice;
    BigDecimal savedLimit = savedGasPreference.gasLimit;

    final GasPriceLimitsGwei gasPriceLimitsGwei =
        viewModel.convertPriceLimitsToGwei(gasPrice, gasPriceMin, gasLimitMax, networkFeeMax);
    setPriceValue(savedGasPrice, gasPriceLimitsGwei);
    setLimitValue(savedLimit, gasLimit);

    setPriceSlider(gasPriceLimitsGwei, savedGasPrice);
    setLimitSlider(gasLimitMax, gasLimitMin, gasLimit, savedLimit);
  }

  private void setLimitValue(BigDecimal savedGasLimit, BigDecimal gasLimit) {
    if (savedGasLimit != null) {
      viewModel.gasLimit()
          .setValue(savedGasLimit);
    } else {
      viewModel.gasLimit()
          .setValue(gasLimit);
    }
  }

  private void setPriceValue(BigDecimal savedGasPrice, GasPriceLimitsGwei gasPriceLimitsGwei) {
    if (isSavedLimitInRange(savedGasPrice, gasPriceLimitsGwei.getMin(),
        gasPriceLimitsGwei.getMax())) {
      viewModel.gasPrice()
          .setValue(savedGasPrice);
    } else {
      viewModel.gasPrice()
          .setValue(gasPriceLimitsGwei.getPrice());
    }
  }

  private void setLimitSlider(BigDecimal gasLimitMax, BigDecimal gasLimitMin, BigDecimal gasLimit,
      BigDecimal savedGasLimit) {
    gasLimitSlider.setMax(gasLimitMax.subtract(gasLimitMin)
        .intValue());
    if (isSavedLimitInRange(savedGasLimit, gasLimitMin, gasLimitMax)) {
      gasLimitSlider.setProgress(savedGasLimit.intValue());
    } else {
      gasLimitSlider.setProgress(gasLimit.subtract(gasLimitMin)
          .intValue());
    }
    gasLimitSlider.refreshDrawableState();
    gasLimitSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = progress / 100;
        progress = progress * 100;
        viewModel.gasLimit()
            .setValue(BigDecimal.valueOf(progress)
                .add(gasLimitMin));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private boolean isSavedLimitInRange(BigDecimal savedValue, BigDecimal minValue,
      BigDecimal maxValue) {
    return savedValue != null
        && savedValue.compareTo(minValue) > 0
        && savedValue.compareTo(maxValue) < 0;
  }

  private void setPriceSlider(GasPriceLimitsGwei gasPriceLimitsGwei, BigDecimal savedGasPrice) {
    int gasPriceProgress;
    BigDecimal gasPriceMaxGwei = gasPriceLimitsGwei.getMax();
    BigDecimal gasPriceMinGwei = gasPriceLimitsGwei.getMin();
    gasPriceSlider.setMax(gasPriceMaxGwei.intValue());
    if (isSavedLimitInRange(savedGasPrice, gasPriceMinGwei, gasPriceMaxGwei)) {
      gasPriceProgress = savedGasPrice.intValue();
    } else {
      gasPriceProgress = gasPriceLimitsGwei.getPrice()
          .subtract(BigDecimal.valueOf(gasPriceMinGwei.intValue()))
          .intValue();
    }
    gasPriceSlider.setProgress(gasPriceProgress);
    gasPriceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        viewModel.gasPrice()
            .setValue(BigDecimal.valueOf(progress + gasPriceMinGwei.intValue()));
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  @Override public void onResume() {

    super.onResume();

    viewModel.prepare();
    sendPageViewEvent();
  }

  private void onDefaultNetwork(NetworkInfo network) {
    gasPriceInfoText.setText(
        getString(R.string.info_gas_price).replace(C.ETHEREUM_NETWORK_NAME, network.name));
    gasLimitInfoText.setText(
        getString(R.string.info_gas_limit).replace(C.ETHEREUM_NETWORK_NAME, network.symbol));
  }

  private void onGasPrice(BigDecimal price) {
    String formattedPrice =
        currencyFormatUtils.formatTransferCurrency(price, WalletCurrency.ETHEREUM)
            + " "
            + C.GWEI_UNIT;
    gasPriceText.setText(formattedPrice);

    updateNetworkFee();
  }

  private void onGasLimit(BigDecimal limit) {
    gasLimitText.setText(limit.toString());

    updateNetworkFee();
  }

  private void updateNetworkFee() {
    BigDecimal fee = BalanceUtils.gweiToWei(BalanceUtils.weiToEth(viewModel.networkFee()));
    String formattedFee = currencyFormatUtils.formatTransferCurrency(fee, WalletCurrency.ETHEREUM)
        + " "
        + C.ETH_SYMBOL;
    networkFeeText.setText(formattedFee);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.send_settings_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }
}
