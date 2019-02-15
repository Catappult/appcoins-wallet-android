package com.asfoundation.wallet.ui.widget.holder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.ui.widget.OnTokenClickListener;

public class TokenChangeHolder extends BinderViewHolder<Token> implements View.OnClickListener {

  public static final int VIEW_TYPE = 1005;
  private final TextView symbol;
  private final Switch enableControl;
  private final View deleteAction;

  private Token token;
  private OnTokenClickListener onTokenClickListener;
  private OnTokenClickListener onTokenDeleteClickListener;

  public TokenChangeHolder(int resId, ViewGroup parent) {
    super(resId, parent);

    symbol = findViewById(R.id.symbol);
    deleteAction = findViewById(R.id.delete_action);
    enableControl = findViewById(R.id.is_enable);
    deleteAction.setOnClickListener(this);
    itemView.setOnClickListener(this);
  }

  @Override public void bind(@Nullable Token data, @NonNull Bundle addition) {
    if (data == null) {
      return;
    }
    token = data;
    if (TextUtils.isEmpty(token.tokenInfo.name)) {
      symbol.setText(token.tokenInfo.symbol);
    } else {
      symbol.setText(token.tokenInfo.name + " (" + token.tokenInfo.symbol + ")");
    }
    if (data.tokenInfo.isAddedManually) {
      deleteAction.setVisibility(View.VISIBLE);
    } else {
      deleteAction.setVisibility(View.GONE);
    }
    enableControl.setChecked(data.tokenInfo.isEnabled);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.delete_action: {
        if (onTokenDeleteClickListener != null) {
          onTokenDeleteClickListener.onTokenClick(v, token);
        }
      }
      break;
      default: {
        if (onTokenClickListener != null) {
          enableControl.setChecked(!token.tokenInfo.isEnabled);
          onTokenClickListener.onTokenClick(v, token);
        }
      }
    }
  }

  public void setOnTokenClickListener(OnTokenClickListener onTokenClickListener) {
    this.onTokenClickListener = onTokenClickListener;
  }

  public void setOnTokenDeleteClickListener(OnTokenClickListener onTokenClickListener) {
    this.onTokenDeleteClickListener = onTokenClickListener;
  }
}
