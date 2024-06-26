package com.asfoundation.wallet.ui.widget.holder;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BinderViewHolder<T> extends RecyclerView.ViewHolder {
  public BinderViewHolder(int resId, ViewGroup parent) {
    super(LayoutInflater.from(parent.getContext())
        .inflate(resId, parent, false));
  }

  public BinderViewHolder(View view) {
    super(view);
  }

  public abstract void bind(@Nullable T data, @NonNull Bundle addition);

  public void bind(@Nullable T data) {
    bind(data, Bundle.EMPTY);
  }

  protected <T extends View> T findViewById(int id) {
    return itemView.findViewById(id);
  }

  protected Context getContext() {
    return itemView.getContext();
  }

  protected String getString(int stringResId) {
    return getContext().getString(stringResId);
  }

  public String getString(int stringResId, Object... args) {
    return getContext().getString(stringResId, args);
  }
}
