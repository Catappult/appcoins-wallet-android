package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupActivity
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.update_required.wallets_list.WalletSelectionAdapter
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.wallets.usecases.GetWalletsModelUseCase
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

  private lateinit var listPopupWindow: ListPopupWindow

  @Inject
  lateinit var buildUpdateIntentUseCase: BuildUpdateIntentUseCase

  @Inject
  lateinit var getCurrentWalletUseCase: GetCurrentWalletUseCase

  @Inject
  lateinit var getWalletsModelUseCase: GetWalletsModelUseCase

  @Inject
  lateinit var rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = IabUpdateRequiredPresenter(
      this,
      CompositeDisposable(),
      buildUpdateIntentUseCase,
      getWalletsModelUseCase,
      rxSchedulers
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
        context = requireContext(),
        walletAddress = walletAddress,
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

  override fun setDropDownMenu(walletsModel: WalletsModel) {
    listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
    listPopupWindow.anchorView = update_required_backup_button
    listPopupWindow.setBackgroundDrawable(
      AppCompatResources.getDrawable(
        requireContext(),
        R.drawable.update_required_backup_wallet_selection
      )
    )

    val adapter = WalletSelectionAdapter(
      requireContext(),
      prepareWalletsList(walletsModel),
      R.layout.wallet_selection_item,
      arrayOf("wallet_name", "wallet_backup_date", "wallet_balance"),
      intArrayOf(
        R.id.wallet_selection_name,
        R.id.wallet_selection_backup_date,
        R.id.wallet_selection_balance,
      )
    )
    listPopupWindow.setAdapter(adapter)

    listPopupWindow.setOnItemClickListener { _, _, position, _ ->
      navigateToBackup(walletsModel.wallets[position].walletAddress)
      listPopupWindow.dismiss()
    }
    listPopupWindow.show()
  }

  private fun prepareWalletsList(walletsModel: WalletsModel): ArrayList<HashMap<String, String>> {
    val arrayList: ArrayList<HashMap<String, String>> = ArrayList()
    for (i in 0 until walletsModel.wallets.size) {
      val hashMap: HashMap<String, String> = HashMap()
      hashMap["wallet_name"] = walletsModel.wallets[i].walletName
      hashMap["wallet_backup_date"] = walletsModel.wallets[i].backupDate.toString()
      hashMap["wallet_balance"] =
        walletsModel.wallets[i].balance.symbol + walletsModel.wallets[i].balance.amount
      arrayList.add(hashMap)
    }
    return arrayList
  }
}