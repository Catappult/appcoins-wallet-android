package com.asfoundation.wallet.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.overlay_fragment.*


class OverlayFragment : Fragment(), OverlayView {

  private lateinit var presenter: OverlayPresenter
  private lateinit var activity: TransactionsActivity

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = OverlayPresenter(this, CompositeDisposable())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is TransactionsActivity) {
      throw IllegalArgumentException(
          OverlayFragment::class.java.simpleName + " needs to be attached to a " + TransactionsActivity::class.java.simpleName)
    }
    activity = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.overlay_fragment, container, false)
  }

  override fun discoverClick(): Observable<Any> {
    return RxView.clicks(discover_button)
  }

  override fun dismissClick(): Observable<Any> {
    return RxView.clicks(dismiss_button)
  }

  override fun dismissView() {
    activity.onBackPressed()
  }

  override fun overlayClick(): Observable<Any> {
    return RxView.clicks(overlay_container)
  }

  override fun navigateToPromotions() {
    activity.navigateToPromotions(true)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
