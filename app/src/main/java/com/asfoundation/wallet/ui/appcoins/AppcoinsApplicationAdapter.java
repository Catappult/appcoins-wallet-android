package com.asfoundation.wallet.ui.appcoins;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.widget.holder.AppcoinsApplicationViewHolder;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import rx.functions.Action1;

public class AppcoinsApplicationAdapter
    extends RecyclerView.Adapter<AppcoinsApplicationViewHolder> {
  private static final String TAG = AppcoinsApplicationAdapter.class.getSimpleName();
  private final Action1<AppcoinsApplication> applicationClickListener;
  private List<AppcoinsApplication> applications;

  public AppcoinsApplicationAdapter(Action1<AppcoinsApplication> applicationClickListener,
      @NotNull List<AppcoinsApplication> applications) {
    this.applicationClickListener = applicationClickListener;
    this.applications = applications;
  }

  @NonNull @Override
  public AppcoinsApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new AppcoinsApplicationViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_appcoins_application, parent, false), applicationClickListener);
  }

  @Override
  public void onBindViewHolder(@NonNull AppcoinsApplicationViewHolder holder, int position) {
    holder.bind(applications.get(position));
  }

  @Override public int getItemCount() {
    return applications.size();
  }

  public void setApplications(List<AppcoinsApplication> applications) {
    this.applications = applications;
    notifyDataSetChanged();
  }
}
