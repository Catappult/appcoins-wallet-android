package com.asfoundation.wallet.ui.appcoins;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.widget.holder.AppcoinsApplicationViewHolder;
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import rx.functions.Action2;

public class AppcoinsApplicationAdapter
    extends RecyclerView.Adapter<AppcoinsApplicationViewHolder> {
  private final Action2<AppcoinsApplication, ApplicationClickAction> applicationClickListener;
  private List<AppcoinsApplication> applications;

  public AppcoinsApplicationAdapter(
      Action2<AppcoinsApplication, ApplicationClickAction> applicationClickListener,
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
