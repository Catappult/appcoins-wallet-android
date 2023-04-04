package com.asfoundation.wallet.ui.settings.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsWalletsLayoutBinding
import com.asfoundation.wallet.ui.settings.wallets.bottomsheet.SettingsWalletsBottomSheetFragment
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsWalletsFragment : Fragment(), SettingsWalletsView {

  @Inject
  lateinit var presenter: SettingsWalletsPresenter
  private lateinit var walletsBottomSheet: BottomSheetBehavior<View>

  private var _binding: SettingsWalletsLayoutBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val bottom_sheet_fragment_container get() = binding.bottomSheetFragmentContainer
  private val faded_background get() = binding.fadedBackground

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
    _binding = SettingsWalletsLayoutBinding.inflate(inflater, container, false)
    return binding.root
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
    if (requireArguments().containsKey(WALLET_MODEL_KEY)) {
      requireArguments().getSerializable(WALLET_MODEL_KEY) as WalletsModel
    } else {
      throw IllegalArgumentException("WalletsModel not available")
    }
  }
}
