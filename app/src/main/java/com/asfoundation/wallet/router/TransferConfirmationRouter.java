package com.asfoundation.wallet.router;

import android.app.Activity;
import android.content.Intent;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.transfers.TransferConfirmationActivity;
import com.appcoins.wallet.core.arch.legacy.ActivityResultSharer;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

import static com.appcoins.wallet.core.utils.jvm_common.C.EXTRA_TRANSACTION_BUILDER;

public class TransferConfirmationRouter implements ActivityResultSharer.ActivityResultListener {

  public static final int TRANSACTION_CONFIRMATION_REQUEST_CODE = 12344;
  private final PublishSubject<Result> result = PublishSubject.create();

  public @Inject TransferConfirmationRouter() {
  }

  public void open(Activity activity, TransactionBuilder transactionBuilder) {
    Intent intent = new Intent(activity, TransferConfirmationActivity.class);
    intent.putExtra(EXTRA_TRANSACTION_BUILDER, transactionBuilder);
    activity.startActivityForResult(intent, TRANSACTION_CONFIRMATION_REQUEST_CODE);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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