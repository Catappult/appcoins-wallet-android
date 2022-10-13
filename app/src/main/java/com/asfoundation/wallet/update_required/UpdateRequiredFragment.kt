package com.asfoundation.wallet.update_required

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.UpdateRequiredFragmentBinding
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpdateRequiredFragment : BasePageViewFragment(),
  SingleStateFragment<UpdateRequiredState, UpdateRequiredSideEffect> {

  @Inject
  lateinit var navigator: UpdateRequiredNavigator

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
    views.updateRequiredBackupButton.setOnClickListener {
      viewModel.handleBackupClick()
    }
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  override fun onStateChanged(state: UpdateRequiredState) = Unit

  override fun onSideEffect(sideEffect: UpdateRequiredSideEffect) {
    when (sideEffect) {
      is UpdateRequiredSideEffect.UpdateActionIntent -> startActivity(sideEffect.intent)
      is UpdateRequiredSideEffect.NavigateToBackup -> navigator.navigateToBackup(sideEffect.walletAddress)
      is UpdateRequiredSideEffect.ShowBackupOption -> shouldShowBackupOption(sideEffect.shouldShow)
    }
  }

  private fun shouldShowBackupOption(shouldShow: Boolean) {
    views.updateRequiredBackupContainer.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }
}

