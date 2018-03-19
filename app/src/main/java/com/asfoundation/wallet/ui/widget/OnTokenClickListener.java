package com.asfoundation.wallet.ui.widget;

import android.view.View;
import com.asfoundation.wallet.entity.Token;

public interface OnTokenClickListener {
  void onTokenClick(View view, Token token);
}
