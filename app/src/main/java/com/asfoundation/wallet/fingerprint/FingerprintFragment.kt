package com.asfoundation.wallet.fingerprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_fingerprint_layout.*
import javax.inject.Inject


class FingerprintFragment : DaggerFragment(), FingerprintView {

  @Inject
  lateinit var preferencesRepositoryType: PreferencesRepositoryType

  companion object {
    fun newInstance(): FingerprintFragment {
      return FingerprintFragment()
    }
  }

  private lateinit var presenter: FingerprintPresenter


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        FingerprintPresenter(this, CompositeDisposable(), preferencesRepositoryType)
  }

  override fun getSwitchClick(): Observable<Boolean> {
    return RxView.clicks(authentication_switch)
        .map { authentication_switch.isChecked }
  }

  override fun setSwitchState(switchState: Boolean) {
    authentication_switch.isChecked = switchState
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_fingerprint_layout, container, false)
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
