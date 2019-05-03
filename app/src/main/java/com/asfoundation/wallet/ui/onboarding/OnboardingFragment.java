package com.asfoundation.wallet.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;
import com.jakewharton.rxbinding2.view.RxView;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OnboardingFragment extends DaggerFragment  implements OnboardingView {

  private OnboardingPresenter presenter;
  private LinearLayout termsConditionsLayout;
  private LottieAnimationView lottieView;
  private Button okButton;
  private Button skipButton;
  private TextView warningText;
  private CheckBox checkbox;
  private ViewPager viewPager;


  public static OnboardingFragment newInstance() {
    Bundle args = new Bundle();
     OnboardingFragment fragment = new OnboardingFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter = new OnboardingPresenter(new CompositeDisposable(), this);
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.layout_onboarding, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    termsConditionsLayout = view.findViewById(R.id.terms_conditions_layout);
    lottieView = view.findViewById(R.id.lottie_onboarding);
    okButton = view.findViewById(R.id.ok_action);
    skipButton = view.findViewById(R.id.skip_action);
    warningText = view.findViewById(R.id.terms_conditions_warning);
    checkbox = view.findViewById(R.id.onboarding_checkbox);
    viewPager = view.findViewById(R.id.intro);

    presenter.present();
  }

  private void showWarningText(int position) {
    if (!checkbox.isChecked() && position == 3) {
      animateShowWarning(warningText);
      warningText.setVisibility(VISIBLE);
    } else {
      if (warningText.getVisibility() == VISIBLE) {
        animateHideWarning(warningText);
        warningText.setVisibility(GONE);
      }
    }
  }

  private void showSkipButton(int position) {
    if (Math.floor(position) != 3 && checkbox.isChecked()) {
      if (skipButton.getVisibility() != VISIBLE) {
        animateShowButton(skipButton);
        animateCheckboxUp(termsConditionsLayout);
        skipButton.setVisibility(VISIBLE);
      }
    } else {
      if (skipButton.getVisibility() == VISIBLE) {
        animateHideButton(skipButton);
        animateCheckboxDown(termsConditionsLayout);
        skipButton.setVisibility(GONE);
      }
    }
  }

  private void showOkButton(int position) {
    if (checkbox.isChecked() && position == 3) {
      animateShowButton(okButton);
      animateCheckboxUp(termsConditionsLayout);
      okButton.setVisibility(VISIBLE);
    } else {
      if (okButton.getVisibility() == VISIBLE) {
        animateHideButton(okButton);
        animateCheckboxDown(termsConditionsLayout);
        okButton.setVisibility(GONE);
      }
    }
  }

  private void animateCheckboxUp(LinearLayout layout) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.minor_translate_up);
    animation.setFillAfter(true);
    layout.setAnimation(animation);
  }

  private void animateCheckboxDown(LinearLayout layout) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.minor_translate_down);
    animation.setFillAfter(true);
    layout.setAnimation(animation);
  }

  private void animateShowButton(Button button) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.bottom_translate_in);
    button.setAnimation(animation);
  }

  private void animateShowWarning(TextView textView) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.fast_fade_in_animation);
    textView.setAnimation(animation);
  }

  private void animateHideButton(Button button) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.bottom_translate_out);
    button.setAnimation(animation);
  }

  private void animateHideWarning(TextView textView) {
    Animation animation =
        AnimationUtils.loadAnimation(getContext(), R.anim.fast_fade_out_animation);
    textView.setAnimation(animation);
  }

  @Override public Observable<Object> getOkClick() {
    return RxView.clicks(okButton);
  }

  @Override public Observable<Object> getSkipClick() {
    return RxView.clicks(skipButton);
  }

  @Override public Observable<Object> getCheckboxClick() {
    return RxView.clicks(checkbox);
  }
}
