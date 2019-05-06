package com.asfoundation.wallet.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.layout_onboarding.*

class OnboardingFragment : DaggerFragment(), OnboardingView {

  private lateinit var presenter: OnboardingPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = OnboardingPresenter(CompositeDisposable(), this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    return inflater.inflate(R.layout.layout_onboarding, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun getOkClick(): Observable<Any> {
    return RxView.clicks(ok_action)
  }

  override fun getSkipClick(): Observable<Any> {
    return RxView.clicks(skip_action)
  }

  override fun getCheckboxClick(): Observable<Any> {
    return RxView.clicks(onboarding_checkbox)
  }

  companion object {
    fun newInstance(): OnboardingFragment {
      return OnboardingFragment()
    }
  }
}