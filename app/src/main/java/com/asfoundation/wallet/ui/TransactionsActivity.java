package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ShareCompat;
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
import com.asfoundation.wallet.rating.RatingActivity;
import com.asfoundation.wallet.referrals.CardNotification;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.overlay.OverlayFragment;
import com.asfoundation.wallet.ui.toolbar.ToolbarArcBackground;
import com.asfoundation.wallet.ui.widget.adapter.TransactionsAdapter;
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel;
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.util.WalletCurrency;
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
import io.intercom.android.sdk.Intercom;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import javax.inject.Inject;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;
import static com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK;
import static com.asfoundation.wallet.ui.bottom_navigation.BottomNavigationItem.BALANCE;
import static com.asfoundation.wallet.ui.bottom_navigation.BottomNavigationItem.PROMOTIONS;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  private static String FROM_APP_OPENING_FLAG = "app_opening_flag";
  private static String maxBonusEmptyScreen;
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  @Inject CurrencyFormatUtils formatter;
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
  private MenuItem vipBadge;
  private View badge;
  private int paddingDp;
  private boolean showScroll = false;
  private View tooltip;
  private PopupWindow popup;
  private View fadedBackground;

  public static Intent newIntent(Context context) {
    return new Intent(context, TransactionsActivity.class);
  }

  public static Intent newIntent(Context context, boolean supportNotificationClicked,
      boolean fromAppOpening) {
    Intent intent = new Intent(context, TransactionsActivity.class);
    intent.putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked);
    intent.putExtra(FROM_APP_OPENING_FLAG, fromAppOpening);
    return intent;
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
    tooltip = getLayoutInflater().inflate(R.layout.fingerprint_tooltip, null);
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
    adapter = new TransactionsAdapter(this::onTransactionClick, null, this::onNotificationClick,
        formatter);

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
    viewModel.shareApp()
        .observe(this, this::shareApp);
    viewModel.shouldShowPromotionsTooltip()
        .observe(this, this::showPromotionsOverlay);
    viewModel.balanceWalletsExperimentAssignment()
        .observe(this, this::changeBottomNavigationName);
    viewModel.shouldShowRateUsDialog()
        .observe(this, this::navigateToRateUs);
    refreshLayout.setOnRefreshListener(() -> viewModel.fetchTransactions(true));

    if (savedInstanceState == null) {
      boolean fromAppOpening = getIntent().getBooleanExtra(FROM_APP_OPENING_FLAG, false);
      if (fromAppOpening) viewModel.increaseTimesInHome();
      boolean supportNotificationClick =
          getIntent().getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false);
      if (supportNotificationClick) {
        overridePendingTransition(0, 0);
        viewModel.showSupportScreen(true);
      }
    }
  }

  public void navigateToRateUs(Boolean shouldNavigate) {
    if (shouldNavigate) {
      Intent intent = RatingActivity.newIntent(this);
      this.startActivityForResult(intent, 0);
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      viewModel.showSettings(this);
    }
    if (item.getItemId() == R.id.action_vip_badge) {
      viewModel.goToVipLink(this);
    }
    return super.onOptionsItemSelected(item);
  }

  private void changeBottomNavigationName(@StringRes Integer name) {
    BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
    bottomNavigationView.getMenu()
        .getItem(BALANCE.getPosition())
        .setTitle(getString(name));
  }

  @Override public void onBackPressed() {
    if (popup != null && popup.isShowing() && fadedBackground != null) {
      dismissPopup();
    } else {
      super.onBackPressed();
    }
  }

  private void shareApp(String url) {
    if (url != null) {
      viewModel.clearShareApp();
      ShareCompat.IntentBuilder.from(this)
          .setText(url)
          .setType("text/plain")
          .setChooserTitle(R.string.share_via)
          .startChooser();
    }
  }

  private void prepareNotificationIcon() {
    BottomNavigationMenuView bottomNavigationMenuView =
        (BottomNavigationMenuView) ((BottomNavigationView) findViewById(
            R.id.bottom_navigation)).getChildAt(0);
    View promotionsIcon = bottomNavigationMenuView.getChildAt(PROMOTIONS.getPosition());
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
    if (supportActionView == null) {
      return;
    }
    LottieAnimationView animation = findViewById(R.id.intercom_animation);

    if (hasMessages && !animation.isAnimating()) {
      animation.playAnimation();
    } else {
      animation.cancelAnimation();
      animation.setProgress(0);
    }

    animation.setOnClickListener(v -> viewModel.showSupportScreen(false));
  }

  private void onFetchTransactionsError(Double maxBonus) {
    if (emptyView == null) {
      emptyView =
          new EmptyTransactionsView(this, String.valueOf(maxBonus), emptyTransactionsSubject, this,
              disposables);
      systemView.showEmpty(emptyView);
    }
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
    boolean supportNotificationClick =
        getIntent().getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false);
    if (!supportNotificationClick) {
      emptyView = null;
      if (disposables.isDisposed()) {
        disposables = new CompositeDisposable();
      }
      adapter.clear();
      list.setVisibility(View.GONE);
      viewModel.prepare();
      viewModel.updateConversationCount();
      viewModel.handleUnreadConversationCount();
      checkRoot();
      Intercom.client()
          .handlePushMessage();
    } else {
      finish();
    }
    sendPageViewEvent();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_transactions_activity, menu);
    supportActionView = menu.findItem(R.id.action_support);
    viewModel.handleUnreadConversationCount();
    viewModel.shouldShowFingerprintTooltip()
        .observe(this, this::showFingerprintTooltip);
    viewModel.handleFingerprintTooltipVisibility(getPackageName());
    setupVipBadge(menu);
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
      alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
          .setTextColor(ResourcesCompat.getColor(getResources(), R.color.text_button_color, null));
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

  private void setTooltip() {
    View settingsView = findViewById(R.id.action_settings);
    if (settingsView != null) {
      popup = new PopupWindow(tooltip);
      popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
      popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
      int yOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f,
          getResources().getDisplayMetrics());
      fadedBackground = findViewById(R.id.faded_background);
      fadedBackground.setVisibility(View.VISIBLE);
      popup.showAsDropDown(settingsView, 0, yOffset * -1);
      setTooltipListeners();
      viewModel.onFingerprintTooltipShown();
    }
  }

  private void setTooltipListeners() {
    tooltip.findViewById(R.id.tooltip_later_button)
        .setOnClickListener(v -> dismissPopup());
    Context context = this;
    tooltip.findViewById(R.id.tooltip_turn_on_button)
        .setOnClickListener(v -> {
          dismissPopup();
          viewModel.onTurnFingerprintOnClick(context);
        });
  }

  private void dismissPopup() {
    viewModel.onFingerprintDismissed();
    fadedBackground.setVisibility(View.GONE);
    popup.dismiss();
  }

  private void onBalanceChanged(GlobalBalance globalBalance) {
    if (globalBalance.getFiatValue()
        .length() > 0 && !globalBalance.getFiatSymbol()
        .isEmpty()) {
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
      String creditsString =
          formatter.formatCurrency(creditsBalance.getValue(), WalletCurrency.CREDITS)
              + " "
              + WalletCurrency.CREDITS.getSymbol();
      stringBuilder.append(creditsString)
          .append(bullet);
    }
    if (showAppcoins) {
      String appcString =
          formatter.formatCurrency(appcoinsBalance.getValue(), WalletCurrency.APPCOINS)
              + " "
              + WalletCurrency.APPCOINS.getSymbol();
      stringBuilder.append(appcString)
          .append(bullet);
    }
    if (showEthereum) {
      String ethString =
          formatter.formatCurrency(ethereumBalance.getValue(), WalletCurrency.ETHEREUM)
              + " "
              + WalletCurrency.ETHEREUM.getSymbol();
      stringBuilder.append(ethString)
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

  public void navigateToPromotions(boolean clearStack) {
    if (clearStack) {
      getSupportFragmentManager().popBackStack();
    }
    viewModel.navigateToPromotions(this);
  }

  private void showPromotionsOverlay(Boolean shouldShow) {
    if (shouldShow) {
      getSupportFragmentManager().beginTransaction()
          .setCustomAnimations(R.anim.fragment_fade_in_animation,
              R.anim.fragment_fade_out_animation, R.anim.fragment_fade_in_animation,
              R.anim.fragment_fade_out_animation)
          .add(R.id.container, OverlayFragment.newInstance(PROMOTIONS.getPosition()))
          .addToBackStack(OverlayFragment.class.getName())
          .commit();
      viewModel.onPromotionsShown();
    }
  }

  private void showFingerprintTooltip(Boolean shouldShow) {
    //Handler is needed otherwise the view returned by findViewById(R.id.action_settings) is null
    if (shouldShow) new Handler().post(this::setTooltip);
  }

  private void setupVipBadge(Menu menu) {
    vipBadge = menu.findItem(R.id.action_vip_badge);
    vipBadge.setVisible(false);
    View vipBadgeActionView = vipBadge.getActionView();
    vipBadgeActionView.setOnClickListener(v -> onOptionsItemSelected(vipBadge));
    viewModel.verifyUserLevel();
    viewModel.shouldShowVipBadge()
        .observe(this, this::showVipBadge);
  }

  private void showVipBadge(Boolean shouldShow) {
    vipBadge.setVisible(shouldShow);
  }

  private void dismissNotification(CardNotification cardNotification) {
    showScroll = adapter.removeItem(cardNotification);
    if (showScroll) {
      viewModel.fetchTransactions(false);
    }
  }
}