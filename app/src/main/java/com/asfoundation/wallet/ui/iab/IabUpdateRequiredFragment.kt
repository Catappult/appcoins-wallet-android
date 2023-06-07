package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletsModelUseCase
import com.asf.wallet.R
import com.asf.wallet.databinding.IabUpdateRequiredLayoutBinding
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.asfoundation.wallet.update_required.wallets_list.WalletSelectionAdapter
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
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
  lateinit var rxSchedulers: RxSchedulers

  private val views by viewBinding(IabUpdateRequiredLayoutBinding::bind)

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
    views.updateDialogButtons.buyButton.setText(getString(R.string.update_button))
    views.updateDialogButtons.cancelButton.setText(getString(R.string.cancel_button))
    presenter.present()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = IabUpdateRequiredLayoutBinding.inflate(inflater).root

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun updateClick() = RxView.clicks(views.updateDialogButtons.buyButton)

  override fun cancelClick() = RxView.clicks(views.updateDialogButtons.cancelButton)

  override fun backupClick() = RxView.clicks(views.updateRequiredBackupButton)

  override fun navigateToBackup(walletAddress: String) {
    requireContext().startActivity(
      com.appcoins.wallet.feature.backup.ui.BackupActivity.newIntent(
        context = requireContext(),
        walletAddress = walletAddress,
        isBackupTrigger = false
      )
    )
  }

  override fun close() = iabView.close(Bundle())

  override fun showError() =
    Snackbar.make(views.mainLayout, R.string.unknown_error, Snackbar.LENGTH_SHORT)

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  override fun setDropDownMenu(walletsModel: WalletsModel) {
    listPopupWindow = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)
    listPopupWindow.anchorView = views.updateRequiredBackupButton
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