package com.asf.wallet.ui.widget;

import android.view.View;
import com.asf.wallet.entity.Token;

public interface OnTokenClickListener {
  void onTokenClick(View view, Token token);
}
