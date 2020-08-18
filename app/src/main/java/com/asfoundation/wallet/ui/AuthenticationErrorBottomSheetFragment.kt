package com.asfoundation.wallet.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.authentication_error_bottomsheet.*

class AuthenticationErrorBottomSheetFragment :
    BottomSheetDialogFragment(),
    AuthenticationErrorBottomSheetView {

  private lateinit var presenter: AuthenticationErrorBottomSheetPresenter
  private lateinit var authenticationPromptView: AuthenticationPromptView

  private val errorMessage: String by lazy {
    if (arguments!!.containsKey(ERROR_MESSAGE_KEY)) {
      arguments!!.getString(ERROR_MESSAGE_KEY, "")
    } else {
      throw IllegalArgumentException("Error message not found")
    }
  }

  companion object {
    private const val ERROR_MESSAGE_KEY = "error_message"

    fun newInstance(message: String): AuthenticationErrorBottomSheetFragment {
      val fragment = AuthenticationErrorBottomSheetFragment()
      fragment.arguments = Bundle().apply {
        putString(ERROR_MESSAGE_KEY, message)
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

  override fun onStart() {
    super.onStart()
    //behaviour needed to fix bottomsheet landscape mode
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.authentication_error_bottomsheet, container, false)
  }

  override fun getButtonClick() = RxView.clicks(retry_authentication)

  override fun retryAuthentication() {
    authenticationPromptView.onRetryButtonClick()
    dismiss()
  }

  override fun setMessage() {
    authentication_error_message.text = errorMessage
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }


  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)
    authenticationPromptView.closeCancel()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is AuthenticationPromptView) { "AuthenticationErrorBottomSheetFragment must be attached to AuthenticationPromptView" }
    authenticationPromptView = context
  }
}