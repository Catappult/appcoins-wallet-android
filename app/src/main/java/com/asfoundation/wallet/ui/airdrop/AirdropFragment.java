package com.asfoundation.wallet.ui.airdrop;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.asf.wallet.R;
import com.squareup.picasso.Picasso;
import dagger.android.support.DaggerFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import javax.inject.Inject;

public class AirdropFragment extends DaggerFragment implements AirdropView {
  @Inject AirdropInteractor airdropInteractor;
  private ImageView captchaView;
  private AirdropPresenter presenter;

  public static AirdropFragment newInstance() {
    return new AirdropFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter = new AirdropPresenter(this, new CompositeDisposable(), airdropInteractor,
        AndroidSchedulers.mainThread());
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.airdrop_fragment_layout, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    captchaView = view.findViewById(R.id.airdrop_fragment_captcha_image_view);
    presenter.present();
  }

  @Override public void onDestroyView() {
    presenter.stop();
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    presenter.stop();
    super.onDestroy();
  }

  @Override public void showCaptcha(String captchaUrl) {
    Picasso.with(getContext())
        .load(captchaUrl)
        .into(captchaView);
  }

  @Override public Observable<String> getAirdropClick() {
    return Observable.empty();
  }
}
