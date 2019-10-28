package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class IabUpdateRequiredFragment : DaggerFragment(), IabUpdateRequiredView {

  private lateinit var presenter: IabUpdateRequiredPresenter
  private lateinit var iabView: IabView
  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract
  private lateinit var externalBrowserRouter: ExternalBrowserRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    externalBrowserRouter = ExternalBrowserRouter()
    presenter = IabUpdateRequiredPresenter(this, CompositeDisposable(), autoUpdateInteract)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "IabUpdateRequired fragment must be attached to IAB activity" }
    iabView = context
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.iab_update_required_layout, container, false)
  }

  override fun navigateToStoreAppView(url: String) {
    context?.let { externalBrowserRouter.open(it, Uri.parse(url)) }
  }

  override fun showError() {

  }

  override fun updateClick(): Observable<Any> {
    return Observable.just(Any())
  }

  override fun cancelClick(): Observable<Any> {
    return Observable.just(Any())
  }

  override fun close() {
    iabView.close(Bundle())
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}