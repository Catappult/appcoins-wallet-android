package com.asfoundation.wallet.ui.widget;

import android.view.View;
import com.asfoundation.wallet.transactions.Operation;
import com.asfoundation.wallet.transactions.Transaction;

public interface OnMoreClickListener {
  void onTransactionClick(View view, Operation operation);
}
