package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.AddTokenInteract;
import com.asfoundation.wallet.poa.TransactionFactory;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.util.BalanceUtils;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.viewmodel.BaseNavigationActivity;
import com.asfoundation.wallet.viewmodel.TransactionsViewModel;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.asfoundation.wallet.widget.DepositView;
import com.asfoundation.wallet.widget.EmptyTransactionsView;
import com.asfoundation.wallet.widget.SystemView;
import dagger.android.AndroidInjection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  public static final String READ_MORE_INFO_URL = "https://www.appstorefoundation.org/readmore";
  private static final String TAG = TransactionsActivity.class.getSimpleName();
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  @Inject AddTokenInteract addTokenInteract;
  @Inject TransactionFactory transactionFactory;
  private TransactionsViewModel viewModel;
  private SystemView systemView;
  private TransactionsAdapter adapter;
  private Dialog dialog;
  private EmptyTransactionsView emptyView;
  private RecyclerView list;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_transactions);

    toolbar();
    enableDisplayHomeAsUp();

    ((AppBarLayout) findViewById(R.id.app_bar)).addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          float percentage =
              ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
          findViewById(R.id.toolbar_layout_logo).setAlpha(1 - (percentage * 1.20f));
          ((ToolbarArcBackground) findViewById(R.id.toolbar_background_arc)).setScale(percentage);
        });

    setCollapsingTitle(new SpannableString(getString(R.string.unknown_balance_with_symbol)));
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
    viewModel.error()
        .observe(this, this::onError);
    viewModel.defaultNetwork()
        .observe(this, this::onDefaultNetwork);
    viewModel.defaultWalletBalance()
        .observe(this, this::onBalanceChanged);
    viewModel.defaultWallet()
        .observe(this, this::onDefaultWallet);
    viewModel.transactions()
        .observe(this, this::onTransactions);
    viewModel.applications()
        .observe(this, this::onApplications);
    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings: {
        viewModel.showSettings(this);
      }
      break;
      case R.id.action_deposit: {
        openExchangeDialog();
      }
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void onApplicationClick(AppcoinsApplication appcoinsApplication) {
    viewModel.onAppClick(appcoinsApplication, this);
  }

  private void onApplications(List<AppcoinsApplication> appcoinsApplications) {
    adapter.setApps(appcoinsApplications);
    showList();
  }

  private void onBalanceChanged(Map<String, String> balance) {
    if (!balance.isEmpty()) {
      Map.Entry<String, String> entry = balance.entrySet()
          .iterator()
          .next();
      String currency = entry.getKey();
      String value = entry.getValue();
      int smallTitleSize = (int) getResources().getDimension(R.dimen.title_small_text);
      int color = getResources().getColor(R.color.appbar_subtitle_color);
      setCollapsingTitle(BalanceUtils.formatBalance(value, currency, smallTitleSize, color));
    }
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
    setCollapsingTitle(new SpannableString(getString(R.string.unknown_balance_without_symbol)));
    adapter.clear();
    list.setVisibility(View.GONE);
    viewModel.prepare();
    checkRoot();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_settings, menu);

    NetworkInfo networkInfo = viewModel.defaultNetwork()
        .getValue();
    if (networkInfo != null && networkInfo.name.equals(ETHEREUM_NETWORK_NAME)) {
      getMenuInflater().inflate(R.menu.menu_deposit, menu);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTransactions(true);
        break;
      }
      case R.id.action_air_drop: {
        viewModel.showAirDrop(this);
        break;
      }
      case R.id.action_learn_more:
        openLearnMore();
        break;
    }
  }

  private void openLearnMore() {
    viewModel.onLearnMoreClick(this, Uri.parse(READ_MORE_INFO_URL));
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
    invalidateOptionsMenu();
  }

  private void showList() {
    // the value is 1 because apps list item is always added, so if there is at least 1
    // transaction, the list is shown
    if (adapter.getItemCount() > 1) {
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
        emptyView = new EmptyTransactionsView(this, this);
      }
      systemView.showEmpty(emptyView);
    }
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
}