package com.asfoundation.wallet.ui.backup.entry

import android.animation.LayoutTransition
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentBackupWalletLayoutBinding
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupWalletFragment : BasePageViewFragment(),
  SingleStateFragment<BackupWalletState, BackupWalletSideEffect> {

  @Inject
  lateinit var backupWalletViewModelFactory: BackupWalletViewModelFactory

  @Inject
  lateinit var navigator: BackupWalletNavigator

  private val viewModel: BackupWalletViewModel by viewModels { backupWalletViewModelFactory }
  private val views by viewBinding(FragmentBackupWalletLayoutBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"

    @JvmStatic
    fun newInstance(walletAddress: String): BackupWalletFragment {
      return BackupWalletFragment()
        .apply {
          arguments = Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_backup_wallet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setTextWatchers()
    setTransitionListener()
    setPasswordToggleListener()

    views.backupBtn.setOnClickListener {
      var password = ""
      if (views.passwordToggle?.backupPasswordToggle?.isChecked == true) {
        password = views.passwordToggle!!.backupPasswordInput.getText()
      }
      navigator.showBackupCreationScreen(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!, password
      )
    }
    views.backupSkipBtn.setOnClickListener {
      navigator.navigateToSkipScreen()
    }

    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
  }

  private fun setPasswordToggleListener() {
    views.passwordToggle?.backupPasswordToggle?.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        views.passwordToggle?.passwordGroup?.isVisible = true
        handlePasswordFields()
      } else {
        views.passwordToggle?.passwordGroup?.isVisible = false
        views.backupBtn.isEnabled = true
      }
    }
  }

  private fun setTextWatchers() {
    val textWatcher = object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
      override fun afterTextChanged(s: Editable) {
        handlePasswordFields()
      }
    }

    views.passwordToggle?.backupPasswordInput?.addTextChangedListener(textWatcher)
    views.passwordToggle?.backupRepeatPasswordInput?.addTextChangedListener(textWatcher)
  }

  private fun handlePasswordFields() {
    val password = views.passwordToggle?.backupPasswordInput?.getText()
    val repeatedPassword = views.passwordToggle?.backupRepeatPasswordInput?.getText()

    if (password!!.isEmpty() || repeatedPassword!!.isEmpty()) {
      showPasswordError(false)
      views.backupBtn.isEnabled = false
    } else if (password.isNotEmpty() && password != repeatedPassword) {
      showPasswordError(true)
      views.backupBtn.isEnabled = false
    } else {
      showPasswordError(false)
      views.backupBtn.isEnabled = true
    }
  }

  private fun showPasswordError(shouldShow: Boolean) {
    var errorMessage: String? = null

    if (shouldShow) {
      errorMessage = getString(R.string.backup_additional_security_password_not_march)
    }
    views.passwordToggle?.backupRepeatPasswordInput?.setError(errorMessage)
  }

  private fun setTransitionListener() {
    views.passwordToggle?.backupPasswordToggleLayout?.layoutTransition?.addTransitionListener(object :
      LayoutTransition.TransitionListener {
      override fun startTransition(
        transition: LayoutTransition?, container: ViewGroup?,
        view: View?, transitionType: Int
      ) = Unit

      override fun endTransition(
        transition: LayoutTransition?, container: ViewGroup?,
        view: View?, transitionType: Int
      ) {
        if (transitionType == LayoutTransition.APPEARING) {
          views.backupScrollView.smoothScrollTo(
            views.backupScrollView.x.toInt(),
            views.backupScrollView.bottom
          )
        }
      }
    })
  }

  override fun onStateChanged(state: BackupWalletState) {
    views.walletBackupInfo.backupWalletAddress.text = state.walletAddress

    when (state.balanceAsync) {
      is Async.Success -> {
        setBalance(state.balanceAsync()!!)
      }
      else -> Unit
    }
  }

  private fun setBalance(balance: Balance) {
    views.walletBackupInfo.backupBalance.text =
      getString(R.string.value_fiat, balance.symbol, balance.amount)
  }

  override fun onSideEffect(sideEffect: BackupWalletSideEffect) = Unit
}
