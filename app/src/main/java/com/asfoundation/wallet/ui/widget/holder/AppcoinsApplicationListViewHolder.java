package com.asfoundation.wallet.ui.widget.holder;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.appcoins.AppcoinsApplicationAdapter;
import com.asfoundation.wallet.ui.appcoins.ItemDecorator;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import java.util.ArrayList;
import java.util.List;
import rx.functions.Action1;

public class AppcoinsApplicationListViewHolder extends BinderViewHolder<List<AppcoinsApplication>> {
  public static final int VIEW_TYPE = 1006;
  private final AppcoinsApplicationAdapter adapter;
  private final RecyclerView recyclerView;
  private final View title;
  private final View icon;

  public AppcoinsApplicationListViewHolder(int resId, ViewGroup parent,
      Action1<AppcoinsApplication> applicationClickListener) {
    super(resId, parent);
    recyclerView = findViewById(R.id.recycler_view);
    title = findViewById(R.id.title);
    icon = findViewById(R.id.icon);
    int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
        getContext().getResources()
            .getDisplayMetrics());
    recyclerView.addItemDecoration(new ItemDecorator(space));
    recyclerView.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    adapter = new AppcoinsApplicationAdapter(applicationClickListener, new ArrayList<>());
    recyclerView.setAdapter(adapter);
  }

  @Override public void bind(@Nullable List<AppcoinsApplication> data, @NonNull Bundle addition) {
    if (data.isEmpty()) {
      recyclerView.setVisibility(View.GONE);
      title.setVisibility(View.GONE);
      icon.setVisibility(View.GONE);
    } else {
      adapter.setApplications(data);
      title.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.VISIBLE);
      icon.setVisibility(View.VISIBLE);
    }
  }
}
