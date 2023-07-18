package com.asfoundation.wallet.ui.settings.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsWalletsLayoutBinding
import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletView
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupEntryChooseWalletFragment : Fragment(), BackupEntryChooseWalletView {

  @Inject
  lateinit var presenter: SettingsWalletsPresenter
  private lateinit var walletsBottomSheet: BottomSheetBehavior<View>

  private val views by viewBinding(SettingsWalletsLayoutBinding::bind)

  companion object {
    private const val WALLET_MODEL_KEY = "wallet_model"

    @JvmStatic
    fun newInstance(walletsModel: WalletsModel): BackupEntryChooseWalletFragment {
      return BackupEntryChooseWalletFragment().apply {
        arguments = Bundle().apply {
          putSerializable(WALLET_MODEL_KEY, walletsModel)
        }
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    /*
    childFragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fragment_slide_up, R.anim.fragment_slide_down,
            R.anim.fragment_slide_up, R.anim.fragment_slide_down)
        .replace(R.id.bottom_sheet_fragment_container,
            SettingsWalletsBottomSheetFragment.newInstance(walletsModel))
        .commit()

     */
    return SettingsWalletsLayoutBinding.inflate(layoutInflater).root

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    walletsBottomSheet = BottomSheetBehavior.from(views.bottomSheetFragmentContainer)
    presenter.present()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    views.fadedBackground.animation =
        AnimationUtils.loadAnimation(context, R.anim.fast_100s_fade_out_animation)
    views.fadedBackground.visibility = View.GONE
    presenter.stop()
  }

  override fun showBottomSheet() {
    walletsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    walletsBottomSheet.isFitToContents = true
    walletsBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
          walletsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    })
  }

  override fun outsideOfBottomSheetClick() = RxView.clicks(views.fadedBackground)

  private val walletsModel: WalletsModel by lazy {
    if (requireArguments().containsKey(WALLET_MODEL_KEY)) {
      requireArguments().getSerializable(WALLET_MODEL_KEY) as WalletsModel
    } else {
      throw IllegalArgumentException("WalletsModel not available")
    }
  }
}
