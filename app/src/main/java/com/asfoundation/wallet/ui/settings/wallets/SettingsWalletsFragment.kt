package com.asfoundation.wallet.ui.settings.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.wallets.bottomsheet.SettingsWalletsBottomSheetFragment
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_balance.*
import javax.inject.Inject

class SettingsWalletsFragment : DaggerFragment(), SettingsWalletsView {

  @Inject
  lateinit var presenter: SettingsWalletsPresenter
  private lateinit var walletsBottomSheet: BottomSheetBehavior<View>

  companion object {
    private const val WALLET_MODEL_KEY = "wallet_model"

    @JvmStatic
    fun newInstance(walletsModel: WalletsModel): SettingsWalletsFragment {
      return SettingsWalletsFragment().apply {
        arguments = Bundle().apply {
          putSerializable(WALLET_MODEL_KEY, walletsModel)
        }
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fragment_slide_up, R.anim.fragment_slide_down,
            R.anim.fragment_slide_up, R.anim.fragment_slide_down)
        .replace(R.id.bottom_sheet_fragment_container,
            SettingsWalletsBottomSheetFragment.newInstance(walletsModel))
        .commit()
    return inflater.inflate(R.layout.settings_wallets_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    walletsBottomSheet = BottomSheetBehavior.from(bottom_sheet_fragment_container)
    presenter.present()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    faded_background.animation =
        AnimationUtils.loadAnimation(context, R.anim.fast_100s_fade_out_animation)
    faded_background.visibility = View.GONE
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

  override fun outsideOfBottomSheetClick() = RxView.clicks(faded_background)

  private val walletsModel: WalletsModel by lazy {
    if (arguments!!.containsKey(WALLET_MODEL_KEY)) {
      arguments!!.getSerializable(WALLET_MODEL_KEY) as WalletsModel
    } else {
      throw IllegalArgumentException("WalletsModel not available")
    }
  }
}
