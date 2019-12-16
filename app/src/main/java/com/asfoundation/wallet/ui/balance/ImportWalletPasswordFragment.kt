package com.asfoundation.wallet.ui.balance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ImportWalletPasswordFragment : DaggerFragment(), ImportWalletPasswordView {

  private lateinit var presenter: ImportWalletPasswordPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        ImportWalletPasswordPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.import_wallet_password_layout, container, false)
  }

  companion object {

    private const val KEYSTORE_KEY = "keystore"

    fun newInstance(keystore: String): ImportWalletPasswordFragment {
      val bundle = Bundle()
      val fragment = ImportWalletPasswordFragment()
      bundle.putString(KEYSTORE_KEY, keystore)
      fragment.arguments = bundle
      return fragment
    }

  }

}
