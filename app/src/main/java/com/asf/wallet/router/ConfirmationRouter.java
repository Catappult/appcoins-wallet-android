package com.asf.wallet.router;

import android.app.Activity;
import android.content.Intent;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.ui.ConfirmationActivity;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.asf.wallet.C.EXTRA_TRANSACTION_BUILDER;

public class ConfirmationRouter {

  public static final int TRANSACTION_CONFIRMATION_REQUEST_CODE = 12344;
  private final PublishSubject<Result> result;

  public ConfirmationRouter(PublishSubject<Result> result) {
    this.result = result;
  }

  public void open(Activity activity, TransactionBuilder transactionBuilder) {
    Intent intent = new Intent(activity, ConfirmationActivity.class);
    intent.putExtra(EXTRA_TRANSACTION_BUILDER, transactionBuilder);
    activity.startActivityForResult(intent, TRANSACTION_CONFIRMATION_REQUEST_CODE);
  }

  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == TRANSACTION_CONFIRMATION_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        result.onNext(new Result(true, requestCode, data));
      } else if (resultCode == Activity.RESULT_CANCELED) {
        result.onNext(new Result(false, requestCode, data));
      }
      return true;
    }
    return false;
  }

  public Observable<Result> getTransactionResult() {
    return result.filter(
        result -> result.getRequestCode() == TRANSACTION_CONFIRMATION_REQUEST_CODE);
  }
}