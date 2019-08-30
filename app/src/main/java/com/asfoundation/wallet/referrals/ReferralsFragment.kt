package com.asfoundation.wallet.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ReferralsFragment : DaggerFragment(), ReferralsView {

  private lateinit var presenter: ReferralsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = ReferralsPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread(),
        Schedulers.io())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.referrals_layout, container, false)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
