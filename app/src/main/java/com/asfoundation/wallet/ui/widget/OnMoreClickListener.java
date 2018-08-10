package com.asfoundation.wallet.ui.widget;

import android.view.View;
import com.asfoundation.wallet.transactions.Operation;

public interface OnMoreClickListener {
  void onTransactionClick(View view, Operation operation);
}
