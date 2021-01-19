package com.asfoundation.wallet.subscriptions.cancelsuccess

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_subscription_cancel_success.*
import javax.inject.Inject

class SubscriptionCancelSuccessFragment : DaggerFragment(), SubscriptionCancelSuccessView {

  @Inject
  lateinit var presenter: SubscriptionCancelSuccessPresenter
  private lateinit var activity: SubscriptionView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_cancel_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setBackListener(view)
    presenter.present()
  }

  private fun setBackListener(view: View) {
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          activity.endCancelSubscription()
        }
        true
      }
    }
  }

  override fun getContinueClicks(): Observable<Any> = RxView.clicks(continue_button)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    getActivity()?.title = getString(R.string.subscriptions_title)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    fun newInstance(): SubscriptionCancelSuccessFragment {
      return SubscriptionCancelSuccessFragment()
    }
  }
}