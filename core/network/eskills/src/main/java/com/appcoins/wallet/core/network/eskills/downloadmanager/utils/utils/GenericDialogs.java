/*
 * Copyright (c) 2016.
 * Modified on 16/08/2016.
 */

package com.appcoins.wallet.core.network.eskills.downloadmanager.utils.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

public class GenericDialogs {

  /**
   * Show an AlertDialog with the {@code title} and the {@code message}. The Alert dialog has an
   * "yes" button and a "no" button.
   *
   * @param title Title to apply on AlertDialog
   * @param message Message to asSnack on AlertDialog
   * @param resourceId
   *
   * @return A Observable that shows the dialog when subscribed and return the action made by
   * user. This action is represented by EResponse
   *
   * @see EResponse
   */
  public static Observable<EResponse> createGenericYesNoCancelMessage(@NonNull Context context,
      @Nullable String title, @Nullable String message, int resourceId) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      final AlertDialog dialog =
          new AlertDialog.Builder(new ContextThemeWrapper(context, resourceId)).setTitle(title)
              .setMessage(message)
              .setPositiveButton(android.R.string.yes, (listener, which) -> {
                subscriber.onNext(EResponse.YES);
                subscriber.onCompleted();
              })
              .setNegativeButton(android.R.string.no, (listener, which) -> {
                subscriber.onNext(EResponse.NO);
                subscriber.onCompleted();
              })
              .setOnCancelListener(listener -> {
                subscriber.onNext(EResponse.CANCEL);
                subscriber.onCompleted();
              })
              .create();
      // cleaning up
      subscriber.add(Subscriptions.create(() -> dialog.dismiss()));
      dialog.show();
    })
        .subscribeOn(AndroidSchedulers.mainThread());
  }

  /**
   * Show an AlertDialog with the {@code title} and the {@code message}. The Alert dialog has an
   * "ok" button.
   *
   * @param title Title to apply on AlertDialog
   * @param message Message to asSnack on AlertDialog
   * @param resourceId
   *
   * @return A Observable that shows the dialog when subscribed and return the action made by
   * user. This action is represented by EResponse
   *
   * @see EResponse
   */
  public static Observable<EResponse> createGenericOkCancelMessage(Context context, String title,
      String message, int resourceId) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      final AlertDialog dialog =
          new AlertDialog.Builder(new ContextThemeWrapper(context, resourceId)).setTitle(title)
              .setMessage(message)
              .setPositiveButton(android.R.string.ok, (listener, which) -> {
                subscriber.onNext(EResponse.YES);
                subscriber.onCompleted();
              })
              .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                subscriber.onNext(EResponse.CANCEL);
                subscriber.onCompleted();
              })
              .create();
      // cleaning up
      subscriber.add(Subscriptions.create(() -> dialog.dismiss()));
      dialog.show();
    });
  }

  public static Observable<EResponse> createGenericOkCancelMessageWithColorButton(Context context,
      String title, String message, String okButton, String cancelButton) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title)
          .setMessage(message)
          .setPositiveButton(okButton, (listener, which) -> {
            subscriber.onNext(EResponse.YES);
            subscriber.onCompleted();
          })
          .setNegativeButton(cancelButton, (dialogInterface, i) -> {
            subscriber.onNext(EResponse.CANCEL);
            subscriber.onCompleted();
          })
          .create();
      subscriber.add(Subscriptions.create(() -> dialog.dismiss()));
      dialog.show();
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setTextColor(Color.GRAY);
    });
  }

  /**
   * Show an AlertDialog with the {@code title} and the {@code message}. The Alert dialog has an
   * "ok" button.
   *
   * @param title Title to apply on AlertDialog
   * @param message Message to asSnack on AlertDialog
   * @param resourceId
   *
   * @return A Observable that shows the dialog when subscribed and return the action made by
   * user. This action is represented by EResponse
   *
   * @see EResponse
   */
  public static Observable<EResponse> createGenericOkMessage(Context context, String title,
      String message, int resourceId) {
    return createGenericContinueMessage(context, null, title, message, android.R.string.ok,
        resourceId);
  }

  public static Observable<EResponse> createGenericContinueMessage(Context context,
      @Nullable View view, String title, String message, @StringRes int buttonText,
      int resourceId) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      AlertDialog.Builder builder =
          new AlertDialog.Builder(new ContextThemeWrapper(context, resourceId)).setTitle(title)
              .setMessage(message)
              .setPositiveButton(buttonText, (dialogInterface, i) -> {
                subscriber.onNext(EResponse.YES);
                subscriber.onCompleted();
              });
      if (view != null) {
        builder.setView(view);
      }
      AlertDialog alertDialog = builder.create();
      subscriber.add(Subscriptions.create(() -> alertDialog.dismiss()));
      alertDialog.show();
    });
  }

  public static Observable<EResponse> createGenericContinueCancelMessage(Context context,
      String title, String message, int resourceId) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      final AlertDialog ad =
          new AlertDialog.Builder(new ContextThemeWrapper(context, resourceId)).setTitle(title)
              .setMessage(message)
              .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                subscriber.onNext(EResponse.YES);
                subscriber.onCompleted();
              })
              .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                subscriber.onNext(EResponse.NO);
                subscriber.onCompleted();
              })
              .setOnCancelListener(dialog -> {
                subscriber.onNext(EResponse.CANCEL);
                subscriber.onCompleted();
              })
              .create();
      // cleaning up
      subscriber.add(Subscriptions.create(() -> ad.dismiss()));
      ad.show();
    });
  }

  public static Observable<EResponse> createGenericOkCancelMessage(Context context, String title,
      @StringRes int message, @StringRes int okMessage, @StringRes int cancelMessage,
      int resourceId) {
    return Observable.create((Subscriber<? super EResponse> subscriber) -> {
      final AlertDialog ad =
          new AlertDialog.Builder(new ContextThemeWrapper(context, resourceId)).setTitle(title)
              .setMessage(message)
              .setPositiveButton(okMessage, (dialog, which) -> {
                subscriber.onNext(EResponse.YES);
                subscriber.onCompleted();
              })
              .setNegativeButton(cancelMessage, (dialogInterface, i) -> {
                subscriber.onNext(EResponse.NO);
                subscriber.onCompleted();
              })
              .setOnCancelListener(dialog -> {
                subscriber.onNext(EResponse.CANCEL);
                subscriber.onCompleted();
              })
              .create();
      // cleaning up
      subscriber.add(Subscriptions.create(() -> ad.dismiss()));
      ad.show();
    });
  }

  /**
   * Creates an endless progressDialog to be shown when user is waiting for something
   *
   * @return A ProgressDialog with a please wait message
   */
  public static ProgressDialog createGenericPleaseWaitDialog(Context context, int resourceId) {
    ProgressDialog progressDialog =
        new ProgressDialog(new ContextThemeWrapper(context, resourceId));
    progressDialog.setMessage(context.getString(R.string.please_wait));
    progressDialog.setCancelable(false);
    return progressDialog;
  }

  /**
   * Creates an endless progressDialog to be shown when user is waiting for something
   *
   * @return A ProgressDialog with a configurable message
   */
  public static ProgressDialog createGenericPleaseWaitDialog(Context context, int resourceId,
      String string) {
    ProgressDialog progressDialog =
        new ProgressDialog(new ContextThemeWrapper(context, resourceId));
    progressDialog.setMessage(string);
    progressDialog.setCancelable(false);
    return progressDialog;
  }

  /**
   * Represents the action made by user on the dialog. <li>{@link #YES}</li> <li>{@link #NO}</li>
   * <li>{@link #CANCEL}</li>
   */
  public enum EResponse {

    /**
     * Used when yes/ok button is pressed
     */
    YES,
    /**
     * Used when no/cancel button is pressed
     */
    NO,
    /**
     * Used when user cancels the dialog by pressing back or clicking out of the dialog
     */
    CANCEL,
  }
}
