package com.asfoundation.wallet.subscriptions.success

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_subscription_cancel_success.*
import javax.inject.Inject

class SubscriptionSuccessFragment : DaggerFragment(), SubscriptionSuccessView {

  @Inject
  lateinit var presenter: SubscriptionSuccessPresenter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_cancel_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setBackListener(view)
    presenter.present()
  }

  override fun setupUi(successType: SubscriptionSuccess) {
    when (successType) {
      SubscriptionSuccess.CANCEL -> {
        success_animation.setAnimation(R.raw.subscription_cancel_success)
        update_title.text = getString(R.string.subscriptions_cancel_confirmation_title)
      }
      SubscriptionSuccess.RENEW -> {
        success_animation.setAnimation(R.raw.success_animation)
        update_title.text = getString(R.string.subscriptions_renewed_confirmation_title)
      }
    }
  }

  private fun setBackListener(view: View) {
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          presenter.navigateToListSubscriptions()
        }
        true
      }
    }
  }

  override fun getContinueClicks(): Observable<Any> = RxView.clicks(continue_button)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity?.title = getString(R.string.subscriptions_title)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  enum class SubscriptionSuccess { CANCEL, RENEW }

  companion object {
    const val SUCCESS_TYPE_KEY = "subscription_success_key"

    fun newInstance(successType: SubscriptionSuccess): SubscriptionSuccessFragment {
      return SubscriptionSuccessFragment().apply {
        arguments = Bundle().apply {
          putSerializable(SUCCESS_TYPE_KEY, successType)
        }
      }
    }
  }
}