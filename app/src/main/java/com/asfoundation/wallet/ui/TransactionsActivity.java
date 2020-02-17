package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.asfoundation.wallet.referrals.CardNotification;
import com.asfoundation.wallet.repository.PreferencesRepositoryType;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel;
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.viewmodel.BaseNavigationActivity;
import com.asfoundation.wallet.viewmodel.TransactionsViewModel;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.asfoundation.wallet.widget.EmptyTransactionsView;
import com.asfoundation.wallet.widget.SystemView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  private static String maxBonusEmptyScreen;
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  @Inject PreferencesRepositoryType preferencesRepositoryType;
  private TransactionsViewModel viewModel;
  private SystemView systemView;
  private TransactionsAdapter adapter;
  private EmptyTransactionsView emptyView;
  private RecyclerView list;
  private TextView subtitleView;
  private LottieAnimationView balanceSkeleton;
  private PublishSubject<String> emptyTransactionsSubject;
  private CompositeDisposable disposables;
  private View emptyClickableView;
  private MenuItem supportActionView;
  private View badge;
  private int paddingDp;
  private boolean showScroll = false;

  public static Intent newIntent(Context context) {
    return new Intent(context, TransactionsActivity.class);
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_transactions);

    toolbar();
    enableDisplayHomeAsUp();

    disposables = new CompositeDisposable();

    balanceSkeleton = findViewById(R.id.balance_skeleton);
    balanceSkeleton.setVisibility(View.VISIBLE);
    emptyClickableView = findViewById(R.id.empty_clickable_view);
    emptyClickableView.setVisibility(View.VISIBLE);
    balanceSkeleton.playAnimation();
    subtitleView = findViewById(R.id.toolbar_subtitle);
    AppBarLayout appBar = findViewById(R.id.app_bar);
    appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
      float percentage = ((float) Math.abs(verticalOffset) / appBarLayout.getTotalScrollRange());
      float alpha = 1 - (percentage * 1.20f);
      findViewById(R.id.toolbar_layout_logo).setAlpha(alpha);
      subtitleView.setAlpha(alpha);
      balanceSkeleton.setAlpha(alpha);
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
    prepareNotificationIcon();
    emptyTransactionsSubject = PublishSubject.create();
    paddingDp = (int) (80 * getResources().getDisplayMetrics().density);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    adapter = new TransactionsAdapter(this::onTransactionClick, this::onApplicationClick,
        this::onNotificationClick, getResources());

    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override public void onItemRangeInserted(int positionStart, int itemCount) {
        if (showScroll) {
          linearLayoutManager.smoothScrollToPosition(list, null, 0);
          showScroll = false;
        }
      }
    });
    SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
    systemView = findViewById(R.id.system_view);
    list = findViewById(R.id.list);
    list.setAdapter(adapter);
    list.setLayoutManager(linearLayoutManager);

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
    viewModel.transactionsModel()
        .observe(this, this::onTransactionsModel);
    viewModel.dismissNotification()
        .observe(this, this::dismissNotification);
    viewModel.gamificationMaxBonus()
        .observe(this, this::onGamificationMaxBonus);
    viewModel.shouldShowPromotionsNotification()
        .observe(this, this::onPromotionsNotification);
    viewModel.getUnreadMessages()
        .observe(this, this::updateSupportIcon);
    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));
    handlePromotionsOverlayVisibility();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      viewModel.showSettings(this);
    }
    return super.onOptionsItemSelected(item);
  }

  private void handlePromotionsOverlayVisibility() {
    if (!preferencesRepositoryType.isFirstTimeOnTransactionActivity()) {
      showPromotionsOverlay();
      preferencesRepositoryType.setFirstTimeOnTransactionActivity();
    }
  }

  private void prepareNotificationIcon() {
    BottomNavigationMenuView bottomNavigationMenuView =
        (BottomNavigationMenuView) ((BottomNavigationView) findViewById(
            R.id.bottom_navigation)).getChildAt(0);
    int promotionsIconIndex = 0;
    View promotionsIcon = bottomNavigationMenuView.getChildAt(promotionsIconIndex);
    BottomNavigationItemView itemView = (BottomNavigationItemView) promotionsIcon;
    badge = LayoutInflater.from(this)
        .inflate(R.layout.notification_badge, bottomNavigationMenuView, false);
    badge.setVisibility(View.INVISIBLE);
    itemView.addView(badge);
  }

  private void onPromotionsNotification(boolean shouldShow) {
    if (shouldShow) {
      badge.setVisibility(View.VISIBLE);
    } else {
      badge.setVisibility(View.INVISIBLE);
    }
  }

  private void updateSupportIcon(boolean hasMessages) {
    if (!hasMessages) {
      supportActionView.getActionView()
          .findViewById(R.id.intercom_animation)
          .setVisibility(View.GONE);
      supportActionView.getActionView()
          .findViewById(R.id.intercom_empty)
          .setVisibility(View.VISIBLE);
    } else {
      supportActionView.getActionView()
          .findViewById(R.id.intercom_empty)
          .setVisibility(View.GONE);

      LottieAnimationView view = supportActionView.getActionView()
          .findViewById(R.id.intercom_animation);

      view.setVisibility(View.VISIBLE);
      view.playAnimation();
    }

    supportActionView.getActionView()
        .setOnClickListener(v -> viewModel.showSupportScreen());
  }

  private void onFetchTransactionsError(Double maxBonus) {
    if (emptyView == null) {
      emptyView =
          new EmptyTransactionsView(this, String.valueOf(maxBonus), emptyTransactionsSubject, this,
              disposables);
      systemView.showEmpty(emptyView);
    }
  }

  private void onApplicationClick(AppcoinsApplication appcoinsApplication) {
    viewModel.onAppClick(appcoinsApplication, this);
  }

  private void onTransactionClick(View view, Transaction transaction) {
    viewModel.showDetails(view.getContext(), transaction);
  }

  private void onNotificationClick(CardNotification cardNotification,
      CardNotificationAction cardNotificationAction) {
    viewModel.onNotificationClick(cardNotification, cardNotificationAction, this);
  }

  @Override protected void onPause() {
    super.onPause();
    viewModel.pause();
    disposables.dispose();
  }

  @Override protected void onResume() {
    super.onResume();
    emptyView = null;
    if (disposables.isDisposed()) {
      disposables = new CompositeDisposable();
    }
    adapter.clear();
    list.setVisibility(View.GONE);
    viewModel.prepare();
    checkRoot();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_transactions_activity, menu);
    supportActionView = menu.findItem(R.id.action_support);
    viewModel.handleUnreadConversationCount();
    return super.onCreateOptionsMenu(menu);
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.try_again: {
        viewModel.fetchTransactions(true);
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
      case R.id.action_promotions: {
        navigateToPromotions(false);
        return true;
      }
      case R.id.action_my_address: {
        viewModel.showMyAddress(this);
        return true;
      }
      case R.id.action_balance: {
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

  private void onTransactionsModel(TransactionsModel transactionsModel) {
    adapter.addItems(transactionsModel);
    showList();
  }

  private void showList() {
    if (adapter.getTransactionsCount() > 0) {
      systemView.setVisibility(View.GONE);
      if (list.getPaddingBottom() != paddingDp) {
        //Adds padding when there's transactions
        list.setPadding(0, 0, 0, paddingDp);
      }
      list.setVisibility(View.VISIBLE);
    } else if (adapter.getNotificationsCount() > 0) {
      systemView.setVisibility(View.VISIBLE);
      if (list.getPaddingBottom() != 0) {
        //Removes padding if the there's no transactions
        list.setPadding(0, 0, 0, 0);
      }
      list.setVisibility(View.VISIBLE);
    } else {
      systemView.setVisibility(View.VISIBLE);
      list.setVisibility(View.GONE);
    }
  }

  private void onDefaultWallet(Wallet wallet) {
    adapter.setDefaultWallet(wallet);
  }

  private void onDefaultNetwork(NetworkInfo networkInfo) {
    adapter.setDefaultNetwork(networkInfo);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if ((errorEnvelope.code == EMPTY_COLLECTION || adapter.getItemCount() == 0)) {
      if (emptyView == null) {
        emptyView = new EmptyTransactionsView(this, String.valueOf(maxBonusEmptyScreen),
            emptyTransactionsSubject, this, disposables);
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
      AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.root_title)
          .setMessage(R.string.root_body)
          .setNegativeButton(R.string.ok, (dialog, which) -> {
          })
          .show();
      alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.transparent, null));
    }
  }

  @Override protected void onDestroy() {
    subtitleView = null;
    emptyClickableView = null;
    balanceSkeleton.removeAllAnimatorListeners();
    balanceSkeleton.removeAllUpdateListeners();
    balanceSkeleton.removeAllLottieOnCompositionLoadedListener();
    emptyTransactionsSubject = null;
    disposables.dispose();
    super.onDestroy();
  }

  private void onBalanceChanged(GlobalBalance globalBalance) {
    if (globalBalance.getFiatValue()
        .length() > 0) {
      balanceSkeleton.setVisibility(View.GONE);
      setCollapsingTitle(globalBalance.getFiatSymbol() + globalBalance.getFiatValue());
      setSubtitle(globalBalance);
    }
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

  public Observable<String> getEmptyTransactionsScreenClick() {
    return emptyTransactionsSubject;
  }

  public void navigateToTopApps() {
    viewModel.showTopApps(this);
  }

  public void navigateToPromotions(boolean clearStack) {
    if (clearStack) {
      getSupportFragmentManager().popBackStack();
    }
    viewModel.navigateToPromotions(this);
  }

  public void showPromotionsOverlay() {
    getSupportFragmentManager().beginTransaction()
        .setCustomAnimations(R.anim.fragment_fade_in_animation, R.anim.fragment_fade_out_animation,
            R.anim.fragment_fade_in_animation, R.anim.fragment_fade_out_animation)
        .add(R.id.container, OverlayFragment.newInstance(0))
        .addToBackStack(OverlayFragment.class.getName())
        .commit();
  }

  private void dismissNotification(CardNotification cardNotification) {
    showScroll = adapter.removeItem(cardNotification);
    if (showScroll) {
      viewModel.fetchTransactions(false);
    }
  }
}