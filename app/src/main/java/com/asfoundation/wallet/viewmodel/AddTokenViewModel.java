package com.asfoundation.wallet.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.interact.FindDefaultWalletInteract;
import com.asfoundation.wallet.router.BalanceRouter;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AddTokenViewModel extends BaseViewModel {

  private final AddTokenInteract addTokenInteract;
  private final FindDefaultWalletInteract findDefaultWalletInteract;
  private final BalanceRouter balanceRouter;

  private final MutableLiveData<Boolean> result = new MutableLiveData<>();

  AddTokenViewModel(AddTokenInteract addTokenInteract,
      FindDefaultWalletInteract findDefaultWalletInteract, BalanceRouter balanceRouter) {
    this.addTokenInteract = addTokenInteract;
    this.findDefaultWalletInteract = findDefaultWalletInteract;
    this.balanceRouter = balanceRouter;
  }

  public void save(String address, String symbol, int decimals) {
    addTokenInteract.add(address, symbol, decimals)
        .subscribe(this::onSaved, this::onError);
  }

  private void onSaved() {
    progress.postValue(false);
    result.postValue(true);
  }

  public LiveData<Boolean> result() {
    return result;
  }

  public void showTokens(Context context) {
    findDefaultWalletInteract.find()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(w -> balanceRouter.open(context, w), this::onError);
  }
}
