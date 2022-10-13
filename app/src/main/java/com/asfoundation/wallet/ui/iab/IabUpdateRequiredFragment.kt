package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupActivity
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_buy_buttons.view.*
import kotlinx.android.synthetic.main.iab_update_required_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class IabUpdateRequiredFragment : BasePageViewFragment(), IabUpdateRequiredView {

  private lateinit var presenter: IabUpdateRequiredPresenter
  private lateinit var iabView: IabView

  @Inject
  lateinit var buildUpdateIntentUseCase: BuildUpdateIntentUseCase

  @Inject
  lateinit var getCurrentWalletUseCase: GetCurrentWalletUseCase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = IabUpdateRequiredPresenter(
      this,
      CompositeDisposable(),
      buildUpdateIntentUseCase,
      getCurrentWalletUseCase
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "IabUpdateRequired fragment must be attached to IAB activity" }
    iabView = context
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    update_dialog_buttons.buy_button.setText(getString(R.string.update_button))
    update_dialog_buttons.cancel_button.setText(getString(R.string.cancel_button))
    presenter.present()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.iab_update_required_layout, container, false)
  }

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun updateClick() = RxView.clicks(update_dialog_buttons.buy_button)

  override fun cancelClick() = RxView.clicks(update_dialog_buttons.cancel_button)

  override fun backupClick() = RxView.clicks(update_required_backup_button)

  override fun navigateToBackup(walletAddress: String) {
    requireContext().startActivity(
      BackupActivity.newIntent(
        requireContext(),
        walletAddress,
        isBackupTrigger = false
      )
    )
  }

  override fun close() = iabView.close(Bundle())

  override fun showError() =
    Snackbar.make(main_layout, R.string.unknown_error, Snackbar.LENGTH_SHORT)

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}