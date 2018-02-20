package com.asf.wallet.ui.widget;

import android.view.View;
import com.asf.wallet.entity.Transaction;

public interface OnTransactionClickListener {
  void onTransactionClick(View view, Transaction transaction);
}
