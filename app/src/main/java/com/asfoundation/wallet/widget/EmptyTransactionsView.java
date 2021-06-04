package com.asfoundation.wallet.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import com.asf.wallet.R;
import com.asfoundation.wallet.home.HomeFragment;
import com.asfoundation.wallet.ui.widget.adapter.EmptyTransactionPagerAdapter;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class EmptyTransactionsView extends FrameLayout {

  private static final int MAX_BONUS_STRING_RESOURCE = R.string.gamification_home_body;
  private static final int NUMBER_PAGES = 2;
  private final int[] body = { R.string.home_empty_discover_apps_body, MAX_BONUS_STRING_RESOURCE };

  public EmptyTransactionsView(@NonNull Context context, @NonNull String bonus,
      PublishSubject<String> emptyTransactionsSubject, HomeFragment transactionsActivity,
      CompositeDisposable disposables) {
    super(context);

    LayoutInflater.from(getContext())
        .inflate(R.layout.layout_empty_transactions, this, true);
    ViewPager viewPager = findViewById(R.id.empty_transactions_viewpager);
    int[] action = { R.string.home_empty_discover_apps_button, R.string.gamification_home_button };
    int[] animation =
        { R.raw.carousel_empty_screen_animation, R.raw.transactions_empty_screen_animation };
    EmptyTransactionPagerAdapter pageAdapter =
        new EmptyTransactionPagerAdapter(animation, transformBodyResourceToString(body, bonus),
            action, NUMBER_PAGES, viewPager, emptyTransactionsSubject);
    pageAdapter.randomizeCarouselContent();
    viewPager.setAdapter(pageAdapter);

    disposables.add(transactionsActivity.getEmptyTransactionsScreenClick()
        .doOnNext(string -> {
          if (string.equals(EmptyTransactionPagerAdapter.CAROUSEL_GAMIFICATION)) {
            //transactionsActivity.navigateToPromotions(false);
          }
          if (string.equals(EmptyTransactionPagerAdapter.CAROUSEL_TOP_APPS)) {
            //transactionsActivity.navigateToTopApps();
          }
        })
        .subscribe());
  }

  private String setMaxBonusOnString(String bonus) {
    return getResources().getString(MAX_BONUS_STRING_RESOURCE, bonus);
  }

  private String[] transformBodyResourceToString(int[] bodyArray, String maxBonus) {
    String[] bodyContent = new String[NUMBER_PAGES];
    for (int i = 0; i < bodyArray.length; i++) {
      if (bodyArray[i] == MAX_BONUS_STRING_RESOURCE) {
        bodyContent[i] = setMaxBonusOnString(maxBonus);
      } else {
        bodyContent[i] = getResources().getString(body[i]);
      }
    }
    return bodyContent;
  }
}