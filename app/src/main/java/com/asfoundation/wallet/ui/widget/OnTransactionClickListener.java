package com.asfoundation.wallet.ui.widget;

import android.view.View;
import com.asfoundation.wallet.entity.RawTransaction;

public interface OnTransactionClickListener {
  void onTransactionClick(View view, RawTransaction transaction);
}
