package com.asfoundation.wallet.permissions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_create_wallet_layout.*
import javax.inject.Inject

class CreateWalletFragment : DaggerFragment(), CreateWalletView {
  companion object {
    fun newInstance(): CreateWalletFragment {
      return CreateWalletFragment()
    }
  }

  @Inject
  lateinit var interactor: CreateWalletInteract

  private lateinit var presenter: CreateWalletPresenter

  private lateinit var navigator: CreateWalletNavigator
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = CreateWalletPresenter(this, CompositeDisposable(), interactor)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    when (context) {
      is CreateWalletNavigator -> navigator = context
      else -> throw IllegalArgumentException(
          "${CreateWalletFragment::class} has to be attached to an activity that implements ${CreateWalletNavigator::class}")
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_create_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun getOnCreateWalletClick(): Observable<Any> {
    return RxView.clicks(provide_wallet_create_wallet_button)
  }

  override fun getCancelClick(): Observable<Any> {
    return RxView.clicks(provide_wallet_cancel)
  }

  override fun closeSuccess() {
    navigator.closeSuccess()
  }

  override fun closeCancel() {
    navigator.closeCancel()
  }
}
