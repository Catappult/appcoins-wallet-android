package com.asfoundation.wallet.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import com.asf.wallet.R;

/**
 * Created by Joao Raimundo on 10/05/2018.
 */
public class ToolbarUtil {

  static public SpannableString formatBalance(Context context, String value, String currency) {
    Resources resources = context.getResources();
    String balance = value + " " + currency.toUpperCase();
    SpannableString styledTitle = new SpannableString(balance);
    int currencyIndex = balance.indexOf(currency.toUpperCase());
    int smallTitleSize = (int) resources.getDimension(R.dimen.title_small_text);
    int color = resources.getColor(R.color.appbar_subtitle_color);

    styledTitle.setSpan(new AbsoluteSizeSpan(smallTitleSize), currencyIndex, balance.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    styledTitle.setSpan(new ForegroundColorSpan(color), currencyIndex, balance.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    return styledTitle;
  }
}
