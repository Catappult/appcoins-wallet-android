package com.asfoundation.wallet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.FetchWalletsInteract;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashViewModel extends ViewModel {
  private MutableLiveData<Wallet[]> wallets = new MutableLiveData<>();

  SplashViewModel(FetchWalletsInteract fetchWalletsInteract) {

    fetchWalletsInteract.fetch()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(wallets::postValue, this::onError);
  }

  private void onError(Throwable throwable) {
    wallets.postValue(new Wallet[0]);
  }

  public LiveData<Wallet[]> wallets() {
    return wallets;
  }
}
