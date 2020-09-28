package com.asfoundation.wallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.authentication_error_bottomsheet.*

class AuthenticationErrorBottomSheetFragment : Fragment(), AuthenticationErrorBottomSheetView {

  private lateinit var presenter: AuthenticationErrorBottomSheetPresenter

  private val errorTimer: Long by lazy {
    if (arguments!!.containsKey(ERROR_TIMER_KEY)) {
      arguments!!.getLong(ERROR_TIMER_KEY, 0)
    } else {
      throw IllegalArgumentException("Error message not found")
    }
  }

  companion object {
    private const val ERROR_TIMER_KEY = "error_message"

    fun newInstance(timer: Long): AuthenticationErrorBottomSheetFragment {
      val fragment = AuthenticationErrorBottomSheetFragment()
      fragment.arguments = Bundle().apply {
        putLong(ERROR_TIMER_KEY, timer)
      }
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        AuthenticationErrorBottomSheetPresenter(this, AndroidSchedulers.mainThread(),
            CompositeDisposable())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.authentication_error_bottomsheet, container, false)
  }

  override fun getButtonClick() = RxView.clicks(retry_authentication)

  override fun retryAuthentication() {
    val parent = provideParentFragment()
    parent?.retryAuthentication()
  }

  override fun setMessage() {
    authentication_error_message.text = getString(R.string.fingerprint_failed_body, errorTimer.toString())
  }

  override fun setupUi() {
    val parent = provideParentFragment()
    parent?.showBottomSheet()
  }

  private fun provideParentFragment(): AuthenticationErrorView? {
    if (parentFragment !is AuthenticationErrorView) {
      return null
    }
    return parentFragment as AuthenticationErrorView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}