package com.asfoundation.wallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asf.wallet.databinding.AuthenticationErrorBottomsheetBinding
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class AuthenticationErrorBottomSheetFragment : Fragment(), AuthenticationErrorBottomSheetView {

  private lateinit var presenter: AuthenticationErrorBottomSheetPresenter

  private val errorTimer: Long by lazy {
    if (requireArguments().containsKey(ERROR_TIMER_KEY)) {
      requireArguments().getLong(ERROR_TIMER_KEY, 0)
    } else {
      throw IllegalArgumentException("Error message not found")
    }
  }

  private var _binding: AuthenticationErrorBottomsheetBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val retry_authentication get() = binding.retryAuthentication
  private val authentication_error_message get() = binding.authenticationErrorMessage

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
    _binding = AuthenticationErrorBottomsheetBinding.inflate(inflater, container, false)
    return binding.root
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