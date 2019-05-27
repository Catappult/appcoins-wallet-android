package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.GlobalBalance;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.viewmodel.BaseNavigationActivity;
import com.asfoundation.wallet.viewmodel.TransactionsViewModel;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.asfoundation.wallet.widget.DepositView;
import com.asfoundation.wallet.widget.EmptyTransactionsView;
import com.asfoundation.wallet.widget.SystemView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import dagger.android.AndroidInjection;
import java.util.List;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  private static String maxBonusEmptyScreen;
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  private TransactionsViewModel viewModel;
  private SystemView systemView;
  private TransactionsAdapter adapter;
  private Dialog dialog;
  private EmptyTransactionsView emptyView;
  private RecyclerView list;
  private TextView subtitleView;
  private LottieAnimationView balanceSkeloton;

  public static Intent newIntent(Context context) {
    Intent intent = new Intent(context, TransactionsActivity.class);
    return intent;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_transactions);

    toolbar();
    enableDisplayHomeAsUp();

    balanceSkeloton = findViewById(R.id.balance_skeloton);
    balanceSkeloton.setVisibility(View.VISIBLE);
    balanceSkeloton.playAnimation();
    subtitleView = findViewById(R.id.toolbar_subtitle);
    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          float percentage =
              ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
          float alpha = 1 - (percentage * 1.20f);
          findViewById(R.id.toolbar_layout_logo).setAlpha(alpha);
          subtitleView.setAlpha(alpha);
          balanceSkeloton.setAlpha(alpha);
          ((ToolbarArcBackground) findViewById(R.id.toolbar_background_arc)).setScale(percentage);

          if (percentage == 0) {
            ((ExtendedFloatingActionButton) findViewById(R.id.top_up_btn)).extend();
          } else {
            ((ExtendedFloatingActionButton) findViewById(R.id.top_up_btn)).shrink();
          }
        });

    setCollapsingTitle(" ");
    initBottomNavigation();
    disableDisplayHomeAsUp();

    adapter = new TransactionsAdapter(this::onTransactionClick, this::onApplicationClick);
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
    systemView = findViewById(R.id.system_view);
    list = findViewById(R.id.list);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);

    systemView.attachRecyclerView(list);
    systemView.attachSwipeRefreshLayout(refreshLayout);

    viewModel = ViewModelProviders.of(this, transactionsViewModelFactory)
        .get(TransactionsViewModel.class);
    viewModel.progress()
        .observe(this, systemView::showProgress);
    viewModel.onFetchTransactionsError()
        .observe(this, this::onFetchTransactionsError);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.getDefaultWalletBalance()
        .observe(this, this::onBalanceChanged);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
    viewModel.transactions()
        .observe(this, this::onTransactions);
    viewModel.gamificationMaxBonus()
        .observe(this, this::onGamificationMaxBonus);
    viewModel.applications()
        .observe(this, this::onApplications);
    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings: {
        viewModel.showSettings(this);
        break;
      }
      case R.id.action_level: {
        viewModel.showRewardsLevel(this);
        break;
      }
      case R.id.action_deposit: {
        openExchangeDialog();
        break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  private void onFetchTransactionsError(Double maxBonus) {
    if (emptyView == null) {
      emptyView = new EmptyTransactionsView(this, this, String.valueOf(maxBonus));
      systemView.showEmpty(emptyView);
    }
  }

  private void onApplicationClick(AppcoinsApplication appcoinsApplication) {
    viewModel.onAppClick(appcoinsApplication, this);
  }

  private void onApplications(List<AppcoinsApplication> appcoinsApplications) {
    adapter.setApps(appcoinsApplications);
    showList();
  }

  private void onTransactionClick(View view, Transaction transaction) {
    viewModel.showDetails(view.getContext(), transaction);
  }

  @Override protected void onPause() {
    super.onPause();

    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    viewModel.pause();
  }

  @Override protected void onResume() {
    super.onResume();
    emptyView = null;
    adapter.clear();
    list.setVisibility(View.GONE);
    viewModel.prepare();
    checkRoot();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_transactions_activity, menu);

    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();
    if (networkInfo != null && networkInfo.name.equals(ETHEREUM_NETWORK_NAME)) {
      getMenuInflater().inflate(R.menu.menu_deposit, menu);
    }
    LottieAnimationView view = menu.findItem(R.id.action_level)
        .getActionView()
        .findViewById(R.id.gamification_highlight_animation_view);
    viewModel.shouldShowGamificationAnimation()
        .observe(this, shouldShow -> {
          if (shouldShow) {
            view.playAnimation();
          } else {
            view.cancelAnimation();
          }
        });
    view.setOnClickListener(v -> viewModel.showRewardsLevel(this));
    return super.onCreateOptionsMenu(menu);
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTransactions(true);
        break;
      }
      case R.id.action_learn_more: {
        viewModel.showRewardsLevel(this);
        break;
      }
      case R.id.top_up_btn: {
        viewModel.showTopUp(this);
        break;
      }
      case R.id.empty_clickable_view: {
        viewModel.showTokens(this);
        break;
      }
    }
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_my_address: {
        viewModel.showMyAddress(this);
        return true;
      }
      case R.id.action_my_tokens: {
        viewModel.showTokens(this);
        return true;
      }
      case R.id.action_send: {
        viewModel.showSend(this);
        return true;
      }
    }
    return false;
  }

  private void onTransactions(List<Transaction> transaction) {
    adapter.addTransactions(transaction);
    showList();
  }

  private void showList() {
    // the value is 1 because apps list item is always added, so if there is at least 1
    // transaction, the list is shown
    if (adapter.getItemCount() > 1) {
      systemView.setVisibility(View.GONE);
      list.setVisibility(View.VISIBLE);
    }
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);
    setBottomMenu(R.menu.menu_main_network);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if ((errorEnvelope.code == EMPTY_COLLECTION || adapter.getItemCount() == 0)) {
      if (emptyView == null) {
        emptyView = new EmptyTransactionsView(this, this, String.valueOf(maxBonusEmptyScreen));
        systemView.showEmpty(emptyView);
      }
    }
  }

  private void onGamificationMaxBonus(double bonus) {
    maxBonusEmptyScreen = Double.toString(bonus);
  }

  private void checkRoot() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
    if (RootUtil.isDeviceRooted() && pref.getBoolean("should_show_root_warning", true)) {
      pref.edit()
          .putBoolean("should_show_root_warning", false)
          .apply();
      new AlertDialog.Builder(this).setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok, (dialog, which) -> {
          })
          .show();
    }
  }

  @Override protected void onDestroy() {
    subtitleView = null;
    balanceSkeloton.removeAllAnimatorListeners();
    balanceSkeloton.removeAllUpdateListeners();
    balanceSkeloton.removeAllLottieOnCompositionLoadedListener();
    super.onDestroy();
  }

  private void openExchangeDialog() {
    Wallet wallet = viewModel.defaultWallet()
        .getValue();
    if (wallet == null) {
      Toast.makeText(this, getString(R.string.error_wallet_not_selected), Toast.LENGTH_SHORT)
          .show();
    } else {
      BottomSheetDialog dialog = new BottomSheetDialog(this);
      DepositView view = new DepositView(this, wallet);
      view.setOnDepositClickListener(this::onDepositClick);
      dialog.setContentView(view);
      BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
      dialog.setOnShowListener(d -> behavior.setPeekHeight(view.getHeight()));
      dialog.show();
      this.dialog = dialog;
    }
  }

  private void onDepositClick(View view, Uri uri) {
    viewModel.openDeposit(view.getContext(), uri);
  }

  private void onBalanceChanged(GlobalBalance globalBalance) {
    balanceSkeloton.setVisibility(View.GONE);
    setCollapsingTitle(globalBalance.getFiatSymbol() + globalBalance.getFiatValue());
    setSubtitle(globalBalance);
  }

  private void setSubtitle(GlobalBalance globalBalance) {
    String subtitle =
        buildCurrencyString(globalBalance.getAppcoinsBalance(), globalBalance.getCreditsBalance(),
            globalBalance.getEtherBalance(), globalBalance.getShowAppcoins(),
            globalBalance.getShowCredits(), globalBalance.getShowEthereum());
    subtitleView.setText(Html.fromHtml(subtitle));
  }

  private String buildCurrencyString(Balance appcoinsBalance, Balance creditsBalance,
      Balance ethereumBalance, boolean showAppcoins, boolean showCredits, boolean showEthereum) {
    StringBuilder stringBuilder = new StringBuilder();
    String bullet = "\u00A0\u00A0\u00A0\u2022\u00A0\u00A0\u00A0";
    if (showCredits) {
      stringBuilder.append(creditsBalance.toString())
          .append(bullet);
    }
    if (showAppcoins) {
      stringBuilder.append(appcoinsBalance.toString())
          .append(bullet);
    }
    if (showEthereum) {
      stringBuilder.append(ethereumBalance.toString())
          .append(bullet);
    }
    String subtitle = stringBuilder.toString();
    if (stringBuilder.length() > bullet.length()) {
      subtitle = stringBuilder.substring(0, stringBuilder.length() - bullet.length());
    }
    return subtitle.replace(bullet, "<font color='#ffffff'>" + bullet + "</font>");
  }
}