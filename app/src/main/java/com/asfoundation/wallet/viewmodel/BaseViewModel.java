package com.asfoundation.wallet.viewmodel;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.asfoundation.wallet.C;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.appcoins.wallet.core.utils.common.Log;
import io.reactivex.disposables.CompositeDisposable;

public class BaseViewModel extends ViewModel {

  protected final MutableLiveData<ErrorEnvelope> error = new MutableLiveData<>();
  protected final MutableLiveData<Boolean> progress = new MutableLiveData<>();
  protected CompositeDisposable disposable = new CompositeDisposable();

  @Override protected void onCleared() {
    cancel();
  }

  private void cancel() {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  public LiveData<ErrorEnvelope> error() {
    return error;
  }

  public LiveData<Boolean> progress() {
    return progress;
  }

  protected void onError(Throwable throwable) {
    Log.e("TAG", "Err", throwable);
    if (TextUtils.isEmpty(throwable.getMessage())) {
      error.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, null, throwable));
    } else {
      error.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, throwable.getMessage(), throwable));
    }
  }
}
