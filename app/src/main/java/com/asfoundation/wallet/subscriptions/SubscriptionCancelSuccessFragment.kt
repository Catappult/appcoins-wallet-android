package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_subscription_cancel_success.*

class SubscriptionCancelSuccessFragment : DaggerFragment(), SubscriptionCancelSuccessView {

  private lateinit var presenter: SubscriptionCancelSuccessPresenter
  private lateinit var activity: SubscriptionView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        SubscriptionCancelSuccessPresenter(this, CompositeDisposable(),
            AndroidSchedulers.mainThread())
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_subscription_cancel_success, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.present()
  }

  override fun getContinueClicks(): Observable<Any> {
    return RxView.clicks(continue_button)
  }

  override fun navigateBack() {
    activity.endCancelSubscription()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is SubscriptionView) { SubscriptionCancelSuccessFragment::class.java.simpleName + " needs to be attached to a " + SubscriptionActivity::class.java.simpleName }
    activity = context
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