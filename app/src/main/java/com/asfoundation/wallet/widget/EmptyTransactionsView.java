package com.asfoundation.wallet.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;

public class EmptyTransactionsView extends FrameLayout {

  private final TextView noTransactionsTextView;
  private final TextView earnBonusTextView;
  private final LottieAnimationView noTransactionsAnimationView;

  public EmptyTransactionsView(@NonNull Context context, OnClickListener onClickListener,
      @NonNull String bonus) {
    super(context);

    LayoutInflater.from(getContext())
        .inflate(R.layout.layout_empty_transactions, this, true);

    noTransactionsTextView = findViewById(R.id.no_transactions_text);
    noTransactionsAnimationView = findViewById(R.id.transactions_empty_screen_animation);
    earnBonusTextView = findViewById(R.id.earn_bonus_text);
    earnBonusTextView.setText(getResources().getString(R.string.gamification_home_body, bonus));

    findViewById(R.id.action_learn_more).setOnClickListener(onClickListener);

    noTransactionsTextView.setText(R.string.no_transactions_yet_main);
    noTransactionsAnimationView.playAnimation();
  }
}
