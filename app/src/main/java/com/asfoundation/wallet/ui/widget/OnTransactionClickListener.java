package com.asfoundation.wallet.ui.widget;

import android.view.View;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.transactions.Transaction;

public interface OnTransactionClickListener {
  void onTransactionClick(View view, RawTransaction transaction);
}
