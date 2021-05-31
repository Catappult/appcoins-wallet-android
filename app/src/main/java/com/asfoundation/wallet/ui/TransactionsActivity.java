package com.asfoundation.wallet.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ShareCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asf.wallet.databinding.ActivityTransactionsBinding;
import com.asfoundation.wallet.entity.Balance;
import com.asfoundation.wallet.entity.ErrorEnvelope;
import com.asfoundation.wallet.entity.GlobalBalance;
import com.asfoundation.wallet.rating.RatingActivity;
import com.asfoundation.wallet.referrals.CardNotification;
import com.asfoundation.wallet.transactions.Transaction;
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication;
import com.asfoundation.wallet.ui.overlay.OverlayFragment;
import com.asfoundation.wallet.ui.transactions.HeaderController;
import com.asfoundation.wallet.ui.transactions.TransactionsController;
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel;
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction;
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction;
import com.asfoundation.wallet.util.CurrencyFormatUtils;
import com.asfoundation.wallet.util.ExtensionFunctionUtilsKt;
import com.asfoundation.wallet.util.RootUtil;
import com.asfoundation.wallet.util.WalletCurrency;
import com.asfoundation.wallet.viewmodel.BaseNavigationActivity;
import com.asfoundation.wallet.viewmodel.TransactionsViewModel;
import com.asfoundation.wallet.viewmodel.TransactionsViewModelFactory;
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel;
import com.asfoundation.wallet.widget.EmptyTransactionsView;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dagger.android.AndroidInjection;
import io.intercom.android.sdk.Intercom;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import javax.inject.Inject;
import kotlin.Unit;

import static com.asfoundation.wallet.C.ErrorCode.EMPTY_COLLECTION;
import static com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK;
import static com.asfoundation.wallet.ui.bottom_navigation.BottomNavigationItem.BALANCE;
import static com.asfoundation.wallet.ui.bottom_navigation.BottomNavigationItem.PROMOTIONS;

public class TransactionsActivity extends BaseNavigationActivity implements View.OnClickListener {

  private static String FROM_APP_OPENING_FLAG = "app_opening_flag";
  @Inject TransactionsViewModelFactory transactionsViewModelFactory;
  @Inject CurrencyFormatUtils formatter;
  private TransactionsViewModel viewModel;
  private CompositeDisposable disposables;

  private ActivityTransactionsBinding views;

  private HeaderController headerController;
  private TransactionsController transactionsController;

  private PublishSubject<String> emptyTransactionsSubject;

  private View badge;
  private View tooltip;
  private PopupWindow popup;
  private EmptyTransactionsView emptyView;

  private double maxBonus = 0.0;

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

    views = ActivityTransactionsBinding.inflate(getLayoutInflater());
    setContentView(views.getRoot());

    disposables = new CompositeDisposable();
    tooltip = getLayoutInflater().inflate(R.layout.fingerprint_tooltip, null);
    views.emptyClickableView.setVisibility(View.VISIBLE);
    views.balanceSkeleton.setVisibility(View.VISIBLE);
    views.balanceSkeleton.playAnimation();

    initBottomNavigation();
    disableDisplayHomeAsUp();
    prepareNotificationIcon();
    emptyTransactionsSubject = PublishSubject.create();

    views.systemView.setVisibility(View.GONE);

    initializeLists();
    initializeViewModel();

    views.transactionsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE
            && !views.topUpBtn.isExtended()
            && recyclerView.computeVerticalScrollOffset() == 0) {
          views.topUpBtn.extend();
        }
        super.onScrollStateChanged(recyclerView, newState);
      }

      @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy != 0
            && recyclerView.computeVerticalScrollOffset() > 0
            && views.topUpBtn.isExtended()) {
          views.topUpBtn.shrink();
        }
        super.onScrolled(recyclerView, dx, dy);
      }
    });

    views.topUpBtn.extend();
    views.refreshLayout.setOnRefreshListener(() -> viewModel.updateData());
    views.actionButtonSupport.setOnClickListener(v -> viewModel.showSupportScreen(false));
    views.actionButtonSettings.setOnClickListener(v -> viewModel.showSettings(this));
    views.sendButton.setOnClickListener(v -> viewModel.showSend(this));
    views.receiveButton.setOnClickListener(v -> viewModel.showMyAddress(this));

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

  private void initializeLists() {
    headerController = new HeaderController();
    views.headerRecyclerView.setController(headerController);
    headerController.setAppcoinsAppClickListener(this::onApplicationClick);
    headerController.setCardNotificationClickListener(this::onNotificationClick);

    transactionsController = new TransactionsController();
    transactionsController.setTransactionClickListener(this::onTransactionClick);
    views.transactionsRecyclerView.setController(transactionsController);

    views.systemView.attachRecyclerView(views.transactionsRecyclerView);
    views.systemView.attachSwipeRefreshLayout(views.refreshLayout);
  }

  private void initializeViewModel() {
    viewModel = ViewModelProviders.of(this, transactionsViewModelFactory)
        .get(TransactionsViewModel.class);
    viewModel.progress()
        .observe(this, views.systemView::showProgress);
    viewModel.error()
        .observe(this, this::onError);
    viewModel.getDefaultWalletBalance()
        .observe(this, this::onBalanceChanged);
    viewModel.defaultWalletModel()
        .observe(this, this::onDefaultWallet);
    viewModel.transactionsModel()
        .observe(this, this::onTransactionsModel);
    viewModel.shouldShowPromotionsNotification()
        .observe(this, this::onPromotionsNotification);
    viewModel.getUnreadMessages()
        .observe(this, this::updateSupportIcon);
    viewModel.shareApp()
        .observe(this, this::shareApp);
    viewModel.shouldShowPromotionsTooltip()
        .observe(this, this::showPromotionsOverlay);
    viewModel.shouldShowRateUsDialog()
        .observe(this, this::navigateToRateUs);
    viewModel.shouldShowFingerprintTooltip()
        .observe(this, this::showFingerprintTooltip);
  }

  public void navigateToRateUs(Boolean shouldNavigate) {
    if (shouldNavigate) {
      Intent intent = RatingActivity.newIntent(this);
      this.startActivityForResult(intent, 0);
    }
  }

  private void changeBottomNavigationName(@StringRes Integer name) {
    views.bottomNavigation.getMenu()
        .getItem(BALANCE.getPosition())
        .setTitle(getString(name));
  }
 // TODO: ddd
  //@Override public boolean onOptionsItemSelected(MenuItem item) {
  //  if (item.getItemId() == R.id.action_settings) {
  //    viewModel.showSettings(this);
  //  }
  //  if (item.getItemId() == R.id.action_vip_badge) {
  //    viewModel.goToVipLink(this);
  //  }
  //  return super.onOptionsItemSelected(item);
  //}

  @Override public void onBackPressed() {
    if (popup != null && popup.isShowing()) {
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
    if (hasMessages && !views.intercomAnimation.isAnimating()) {
      views.intercomAnimation.playAnimation();
    } else {
      views.intercomAnimation.cancelAnimation();
      views.intercomAnimation.setProgress(0);
    }
  }

  private Unit onApplicationClick(AppcoinsApplication appcoinsApplication,
      ApplicationClickAction applicationClickAction) {
    viewModel.onAppClick(appcoinsApplication, applicationClickAction, this);
    return Unit.INSTANCE;
  }

  private Unit onTransactionClick(Transaction transaction) {
    viewModel.showDetails(this, transaction);
    return Unit.INSTANCE;
  }

  private Unit onNotificationClick(CardNotification cardNotification,
      CardNotificationAction cardNotificationAction) {
    viewModel.onNotificationClick(cardNotification, cardNotificationAction, this);
    return Unit.INSTANCE;
  }

  @Override protected void onPause() {
    super.onPause();
    viewModel.stopRefreshingData();
    disposables.dispose();
  }

  @Override protected void onResume() {
    super.onResume();
    boolean supportNotificationClick =
        getIntent().getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false);
    if (!supportNotificationClick) {
      if (disposables.isDisposed()) {
        disposables = new CompositeDisposable();
      }
      viewModel.updateData();
      checkRoot();
      Intercom.client()
          .handlePushMessage();
    } else {
      finish();
    }
    sendPageViewEvent();
  }

  @Override public void onClick(View view) {
    int id = view.getId();
    if (view.getId() == R.id.try_again) {
      viewModel.updateData();
    } else if (id == R.id.top_up_btn) {
      viewModel.showTopUp(this);
    } else if (id == R.id.empty_clickable_view) {
      viewModel.showTokens(this);
    }
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.action_promotions) {
      navigateToPromotions(false);
      return true;
    } else if (itemId == R.id.action_my_address) {
      viewModel.showMyAddress(this);
      return true;
    } else if (itemId == R.id.action_balance) {
      viewModel.showTokens(this);
      return true;
    } else if (itemId == R.id.action_send) {
      viewModel.showSend(this);
      return true;
    }
    return false;
  }

  private void onTransactionsModel(Pair<TransactionsModel, TransactionsWalletModel> result) {
    views.transactionsRecyclerView.setVisibility(View.VISIBLE);
    views.systemView.setVisibility(View.GONE);
    transactionsController.setData(result.first, result.second.getWallet(),
        result.second.getNetworkInfo());
    headerController.setData(result.first);

    showList(result.first);
  }

  private void showList(TransactionsModel transactionsModel) {
    views.systemView.showProgress(false);
    if (transactionsModel.getTransactions()
        .size() > 0) {
      views.systemView.setVisibility(View.INVISIBLE);
      views.transactionsRecyclerView.setVisibility(View.VISIBLE);
    } else {
      views.systemView.setVisibility(View.VISIBLE);
      views.transactionsRecyclerView.setVisibility(View.INVISIBLE);
      maxBonus = transactionsModel.getMaxBonus();
      views.systemView.showEmpty(getEmptyView(maxBonus));
    }
    if (transactionsModel.getNotifications()
        .size() > 0
        || transactionsModel.getApplications()
        .size() > 0) {
      views.headerRecyclerView.setVisibility(View.VISIBLE);
      views.spacer.setVisibility(View.VISIBLE);
      views.container.loadLayoutDescription(R.xml.activity_transactions_scene);
    } else {
      if (views.spacer.getVisibility() == View.VISIBLE) {
        views.headerRecyclerView.setVisibility(View.GONE);
        views.spacer.setVisibility(View.GONE);
      }
      views.container.loadLayoutDescription(R.xml.activity_transactions_scene_short);
    }
  }

  private EmptyTransactionsView getEmptyView(double maxBonus) {
    if (emptyView == null) {
      emptyView =
          new EmptyTransactionsView(this, String.valueOf(maxBonus), emptyTransactionsSubject, this,
              disposables);
    }
    return emptyView;
  }

  private void onDefaultWallet(TransactionsWalletModel walletModel) {
    views.transactionsRecyclerView.setVisibility(View.INVISIBLE);
    views.systemView.setVisibility(View.VISIBLE);
    views.systemView.showProgress(true);

    transactionsController = new TransactionsController();
    transactionsController.setTransactionClickListener(this::onTransactionClick);
    views.transactionsRecyclerView.setController(transactionsController);
  }

  private void onError(ErrorEnvelope errorEnvelope) {
    if (errorEnvelope.code == EMPTY_COLLECTION) {
      views.systemView.showEmpty(getEmptyView(maxBonus));
    }
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
    views.balanceSkeleton.removeAllAnimatorListeners();
    views.balanceSkeleton.removeAllUpdateListeners();
    views.balanceSkeleton.removeAllLottieOnCompositionLoadedListener();
    emptyTransactionsSubject = null;
    emptyView = null;
    disposables.dispose();
    super.onDestroy();
  }

  private void setTooltip() {
    popup = new PopupWindow(tooltip);
    popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    int yOffset = ExtensionFunctionUtilsKt.convertDpToPx(36, getResources());
    views.fadedBackground.setVisibility(View.VISIBLE);
    popup.showAsDropDown(views.actionButtonSettings, 0, -yOffset);
    setTooltipListeners();
    viewModel.onFingerprintTooltipShown();
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
    views.fadedBackground.setVisibility(View.GONE);
    popup.dismiss();
  }

  private void onBalanceChanged(GlobalBalance globalBalance) {
    if (globalBalance.getFiatValue()
        .length() > 0 && !globalBalance.getFiatSymbol()
        .isEmpty()) {
      views.balanceSkeleton.setVisibility(View.GONE);
      views.balance.setText(globalBalance.getFiatSymbol() + globalBalance.getFiatValue());
      setCollapsingTitle(globalBalance.getFiatSymbol() + globalBalance.getFiatValue());
      setSubtitle(globalBalance);
    }
  }

  private void setSubtitle(GlobalBalance globalBalance) {
    String subtitle =
        buildCurrencyString(globalBalance.getAppcoinsBalance(), globalBalance.getCreditsBalance(),
            globalBalance.getEtherBalance(), globalBalance.getShowAppcoins(),
            globalBalance.getShowCredits(), globalBalance.getShowEthereum());
    views.balanceSubtitle.setText(Html.fromHtml(subtitle));
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

  public void navigateToTopApps() {
    viewModel.showTopApps(this);
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
    if (shouldShow) {
      this.setTooltip();
    }
  }
}