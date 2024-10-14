package com.asfoundation.wallet.backup.entryBottomSheet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.backup.ui.R
import com.appcoins.wallet.feature.backup.ui.databinding.SettingsWalletBottomSheetLayoutBinding
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.ui.common.addBottomItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class BackupEntryChooseWalletBottomSheetFragment :
  BackupEntryChooseWalletBottomSheetView, BottomSheetDialogFragment(), Navigator {

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: BackupEntryChooseWalletBottomSheetPresenter

  private var uiEventListener: PublishSubject<String>? = null

  private val binding by viewBinding(SettingsWalletBottomSheetLayoutBinding::bind)

  companion object {

    const val WALLET_MODEL_KEY = "wallet_model"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = SettingsWalletBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val arguments = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requireArguments().getSerializable(WALLET_MODEL_KEY, WalletsModel::class.java) as WalletsModel
    } else {
      requireArguments().getSerializable(WALLET_MODEL_KEY) as WalletsModel
    }
    presenter.present(arguments, navController())
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return com.asf.wallet.R.style.AppBottomSheetDialogThemeDraggable
  }

  override fun setupUi(walletsBalance: List<WalletInfoSimple>) {
    with(binding.bottomSheetWalletsCards) {
      addBottomItemDecoration(resources.getDimension(R.dimen.normal_padding))
      isNestedScrollingEnabled = false
      layoutManager = LinearLayoutManager(context).apply { orientation = RecyclerView.VERTICAL }
      adapter = BackupEntryChooseWalletAdapter(walletsBalance, uiEventListener!!, currencyFormatter)
    }
    provideParentFragment()?.showBottomSheet()
  }

  override fun walletCardClicked() = uiEventListener!!

  private fun provideParentFragment(): BackupEntryChooseWalletView? =
    if (parentFragment !is BackupEntryChooseWalletView) {
      null
    } else {
      parentFragment as BackupEntryChooseWalletView
    }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity()
        .supportFragmentManager
        .findFragmentById(com.asf.wallet.R.id.main_host_container) as NavHostFragment
    return navHostFragment.navController
  }
}
