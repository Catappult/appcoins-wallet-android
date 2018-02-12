package com.wallet.crypto.trustapp.ui.widget.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.wallet.crypto.trustapp.R;
import com.wallet.crypto.trustapp.entity.Token;
import com.wallet.crypto.trustapp.ui.widget.OnTokenClickListener;
import com.wallet.crypto.trustapp.ui.widget.holder.TokenChangeHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangeTokenCollectionAdapter extends RecyclerView.Adapter<TokenChangeHolder> {
  private final List<Token> items = new ArrayList<>();

  private final OnTokenClickListener onTokenClickListener;
  private final OnTokenClickListener onTokenDeleteClickListener;

  public ChangeTokenCollectionAdapter(OnTokenClickListener onTokenClickListener,
      OnTokenClickListener onTokenDeleteClickListener) {
    this.onTokenClickListener = onTokenClickListener;
    this.onTokenDeleteClickListener = onTokenDeleteClickListener;
  }

  @Override public TokenChangeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    TokenChangeHolder tokenHolder = new TokenChangeHolder(R.layout.item_change_token, parent);
    tokenHolder.setOnTokenClickListener(onTokenClickListener);
    tokenHolder.setOnTokenDeleteClickListener(onTokenDeleteClickListener);
    return tokenHolder;
  }

  @Override public void onBindViewHolder(TokenChangeHolder holder, int position) {
    holder.bind(items.get(position));
  }

  @Override public int getItemCount() {
    return items.size();
  }

  public void setTokens(Token[] tokens) {
    items.clear();
    items.addAll(Arrays.asList(tokens));
    notifyDataSetChanged();
  }
}
