package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.navigator.UpdateNavigator
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_buy_buttons.view.*
import kotlinx.android.synthetic.main.iab_update_required_layout.*
import javax.inject.Inject

class IabUpdateRequiredFragment : DaggerFragment(), IabUpdateRequiredView {

  private lateinit var presenter: IabUpdateRequiredPresenter
  private lateinit var iabView: IabView
  @Inject
  lateinit var updateNavigator: UpdateNavigator
  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    updateNavigator = UpdateNavigator()
    presenter = IabUpdateRequiredPresenter(this, CompositeDisposable(), autoUpdateInteract)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "IabUpdateRequired fragment must be attached to IAB activity" }
    iabView = context
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    update_dialog_buttons.buy_button.text = getString(R.string.update_button)
    update_dialog_buttons.cancel_button.text = getString(R.string.cancel_button)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.iab_update_required_layout, container, false)
  }

  override fun navigateToStoreAppView(url: String) {
    updateNavigator.navigateToStoreAppView(context, url)
  }

  override fun updateClick(): Observable<Any> {
    return RxView.clicks(update_dialog_buttons.buy_button)
  }

  override fun cancelClick(): Observable<Any> {
    return RxView.clicks(update_dialog_buttons.cancel_button)
  }

  override fun close() {
    iabView.close(Bundle())
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}