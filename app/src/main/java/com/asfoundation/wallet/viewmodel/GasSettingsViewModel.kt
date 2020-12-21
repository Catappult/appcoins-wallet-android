package com.asfoundation.wallet.viewmodel;

import android.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.ui.GasSettingsInteractor;
import java.math.BigDecimal;

public class GasSettingsViewModel extends BaseViewModel {

  public static final int SET_GAS_SETTINGS = 1;

  private MutableLiveData<BigDecimal> gasPrice = new MutableLiveData<>();
  private MutableLiveData<BigDecimal> gasLimit = new MutableLiveData<>();
  private MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
  private GasSettingsInteractor gasSettingsInteractor;

  GasSettingsViewModel(GasSettingsInteractor gasSettingsInteractor) {
    this.gasSettingsInteractor = gasSettingsInteractor;
    gasPrice.setValue(BigDecimal.ZERO);
    gasLimit.setValue(BigDecimal.ZERO);
  }

  public void prepare() {
    disposable = gasSettingsInteractor.find()
        .subscribe(this::onDefaultNetwork, this::onError);
  }

  public MutableLiveData<BigDecimal> gasPrice() {
    return gasPrice;
  }

  public MutableLiveData<BigDecimal> gasLimit() {
    return gasLimit;
  }

  public LiveData<NetworkInfo> defaultNetwork() {
    return defaultNetwork;
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    defaultNetwork.setValue(networkInfo);
  }

  public BigDecimal networkFee() {
    return gasPrice.getValue()
        .multiply(gasLimit.getValue());
  }

  public void saveChanges(BigDecimal gasPrice, BigDecimal gasLimit) {
    gasSettingsInteractor.saveGasPreferences(gasPrice, gasLimit);
  }

  public Pair<BigDecimal, BigDecimal> getGasPreferences() {
    return gasSettingsInteractor.getSavedGasPreferences();
  }
}
