package com.asfoundation.wallet.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import com.asf.wallet.R;

public class EmptyTransactionsView extends FrameLayout {

  private final Button airdropButton;

  public EmptyTransactionsView(@NonNull Context context, OnClickListener onClickListener) {
    super(context);

    LayoutInflater.from(getContext())
        .inflate(R.layout.layout_empty_transactions, this, true);

    airdropButton = findViewById(R.id.action_air_drop);
    findViewById(R.id.action_learn_more).setOnClickListener(onClickListener);
    airdropButton.setOnClickListener(onClickListener);
  }

  public void setAirdropButtonEnable(boolean enabled) {
    airdropButton.setEnabled(enabled);
  }
}
