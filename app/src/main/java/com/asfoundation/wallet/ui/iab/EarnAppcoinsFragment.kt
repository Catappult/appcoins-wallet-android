package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.view.*
import kotlinx.android.synthetic.main.earn_appcoins_layout.*

class EarnAppcoinsFragment : DaggerFragment(), EarnAppcoinsView {

  private lateinit var presenter: EarnAppcoinsPresenter
  private lateinit var iabView: IabView
  private var onBackPressSubject: PublishSubject<Any>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    presenter = EarnAppcoinsPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    onBackPressSubject = PublishSubject.create()
    super.onCreate(savedInstanceState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Earn Appcoins fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    dialog_buy_buttons_payment_methods.buy_button.text = getString(R.string.discover_button)
    dialog_buy_buttons_payment_methods.cancel_button.text = getString(R.string.back_button)
    setBackListener(view)
    presenter.present()
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.earn_appcoins_layout, container, false)
  }

  override fun backButtonClick(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun discoverButtonClick(): Observable<Any> {
    return RxView.clicks(buy_button)
  }

  override fun navigateBack(preSelectedMethod: PaymentMethodsView.SelectedPaymentMethod) {
    iabView.showPaymentMethodsView(preSelectedMethod)
  }

  override fun backPressed(): Observable<Any> {
    return onBackPressSubject!!
  }

  override fun navigateToAptoide() {
    val intent = Intent(Intent.ACTION_VIEW)
    val packageManager = context?.packageManager
    intent.data = Uri.parse(APTOIDE_EARN_APPCOINS_DEEPLINK)
    val appsList = packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    appsList?.forEach {
      if (it.activityInfo.packageName == "cm.aptoide.pt") {
        intent.setPackage(it.activityInfo.packageName)
      }
    }
    iabView.launchIntent(intent)
  }

  private fun setBackListener(view: View) {
    iabView.disableBack()
    view.isFocusableInTouchMode = true
    view.requestFocus()
    view.setOnKeyListener { _, keyCode, keyEvent ->
      if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
        onBackPressSubject?.onNext("")
      }
      true
    }
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.destroy()
    onBackPressSubject = null
    super.onDestroyView()
  }

  companion object {
    const val APTOIDE_EARN_APPCOINS_DEEPLINK = "aptoide://cm.aptoide.pt/deeplink?name=appcoins_ads"
  }
}
