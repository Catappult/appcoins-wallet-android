package com.asfoundation.wallet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.authentication_error_bottomsheet.*

class AuthenticationErrorBottomSheetFragment(private val message: CharSequence) :
    BottomSheetDialogFragment(),
    AuthenticationErrorBottomSheetView {

  private lateinit var presenter: AuthenticationErrorBottomSheetPresenter
  private lateinit var splashView: SplashView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        AuthenticationErrorBottomSheetPresenter(this, AndroidSchedulers.mainThread(),
            Schedulers.io(), CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.authentication_error_bottomsheet, container, false)
  }

  override fun getButtonClick() = RxView.clicks(retry_authentication)

  override fun retryAuthentication() {
    splashView.onRetryButtonClick()
    dismiss()
  }

  override fun setMessage() {
    authentication_error_message.text = message
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is SplashView) { "AuthenticationErrorBottomSheetFragment must be attached to SplashActivity" }
    splashView = context

  }


}