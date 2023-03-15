package com.asfoundation.wallet.update_required

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.UpdateRequiredFragmentBinding
import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.update_required.wallets_list.WalletSelectionAdapter
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpdateRequiredFragment : BasePageViewFragment(),
  SingleStateFragment<UpdateRequiredState, UpdateRequiredSideEffect> {

  @Inject
  lateinit var navigator: UpdateRequiredNavigator

  private lateinit var listPopupWindow: ListPopupWindow

  private val views by viewBinding(UpdateRequiredFragmentBinding::bind)

  private val viewModel: UpdateRequiredViewModel by viewModels()

  companion object {
    @JvmStatic
    fun newInstance(): UpdateRequiredFragment = UpdateRequiredFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.update_required_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.updateButton.setOnClickListener {
      viewModel.handleUpdateClick()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onResume() {
    super.onResume()
    viewModel.checkBackupOption()
  }

  override fun onSideEffect(sideEffect: UpdateRequiredSideEffect) {
    when (sideEffect) {
      is UpdateRequiredSideEffect.UpdateActionIntent -> startActivity(sideEffect.intent)
      is UpdateRequiredSideEffect.NavigateToBackup -> navigator.navigateToBackup(sideEffect.walletAddress)
      is UpdateRequiredSideEffect.ShowBackupOption -> handleBackupOption(sideEffect.walletsModel)
    }
  }

  override fun onStateChanged(state: UpdateRequiredState) {
    when (val walletsModel = state.walletsModel) {
      is Async.Uninitialized,
      is Async.Loading,
      is Async.Fail -> {
        views.updateRequiredBackupContainer.visibility = View.GONE
      }
      is Async.Success -> {
        handleBackupOption(walletsModel())
      }
    }
  }

  private fun handleBackupOption(walletsModel: WalletsModel) {
    views.updateRequiredBackupContainer.visibility =
      if (walletsModel.totalWallets > 0) View.VISIBLE else View.GONE

    views.updateRequiredBackupButton.setOnClickListener {
      when (walletsModel.totalWallets) {
        0 -> Unit
        1 -> viewModel.handleBackupClick(walletsModel.wallets[0].walletAddress)
        else -> setDropDownMenu(walletsModel)
      }
    }
  }

  private fun setDropDownMenu(walletsModel: WalletsModel) {
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
      viewModel.handleBackupClick(walletsModel.wallets[position].walletAddress)
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

