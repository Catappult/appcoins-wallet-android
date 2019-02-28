package com.asfoundation.wallet.ui;

import android.os.Looper;
import androidx.annotation.CheckResult;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposables;

public class RxCheckbox extends Observable<Boolean> {
  private final CheckBox view;

  public RxCheckbox(CheckBox view) {
    this.view = view;
  }

  @CheckResult @NonNull public static Observable<Boolean> checks(@NonNull CheckBox view) {
    if (view == null) {
      throw new NullPointerException("view == null");
    }
    return new RxCheckbox(view);
  }

  @Override protected void subscribeActual(Observer observer) {
    if (!checkMainThread(observer)) {
      return;
    }
    Listener listener = new Listener(view, observer);
    observer.onSubscribe(listener);
    view.setOnCheckedChangeListener(listener);
  }

  private boolean checkMainThread(Observer observer) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      observer.onSubscribe(Disposables.empty());
      observer.onError(new IllegalStateException(
          "Expected to be called on the main thread but was " + Thread.currentThread()
              .getName()));
      return false;
    }
    return true;
  }

  static final class Listener extends MainThreadDisposable
      implements CompoundButton.OnCheckedChangeListener {
    private final CheckBox view;

    private final Observer<? super Object> observer;

    Listener(CheckBox view, Observer<? super Object> observer) {
      this.view = view;
      this.observer = observer;
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      if (!isDisposed()) {
        observer.onNext(isChecked);
      }
    }

    @Override protected void onDispose() {
      view.setOnCheckedChangeListener(null);
    }
  }
}
