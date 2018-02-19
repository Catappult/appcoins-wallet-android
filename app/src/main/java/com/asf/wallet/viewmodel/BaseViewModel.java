package com.asf.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import android.util.Log;
import com.asf.wallet.C;
import com.asf.wallet.entity.ErrorEnvelope;
import io.reactivex.disposables.Disposable;

public class BaseViewModel extends ViewModel {

  protected final MutableLiveData<ErrorEnvelope> error = new MutableLiveData<>();
  protected final MutableLiveData<Boolean> progress = new MutableLiveData<>();
  protected Disposable disposable;

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
    Log.d("TAG", "Err", throwable);
    if (TextUtils.isEmpty(throwable.getMessage())) {
      error.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, null, throwable));
    } else {
      error.postValue(new ErrorEnvelope(C.ErrorCode.UNKNOWN, throwable.getMessage(), throwable));
    }
  }
}
