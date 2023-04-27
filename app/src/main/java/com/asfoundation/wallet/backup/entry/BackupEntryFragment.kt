package com.asfoundation.wallet.backup.entry

import android.animation.LayoutTransition
import android.os.Bundle
import android.os.CountDownTimer
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
import com.asf.wallet.databinding.BackupEntryFragmentBinding
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupEntryFragment : BasePageViewFragment(),
  SingleStateFragment<BackupEntryState, BackupEntrySideEffect> {

  @Inject
  lateinit var backupEntryViewModelFactory: BackupEntryViewModelFactory

  @Inject
  lateinit var navigator: BackupEntryNavigator

  @Inject
  lateinit var walletsEventSender: WalletsEventSender

  private val viewModel: BackupEntryViewModel by viewModels { backupEntryViewModelFactory }
  private val views by viewBinding(BackupEntryFragmentBinding::bind)

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"

    @JvmStatic
    fun newInstance(walletAddress: String): BackupEntryFragment {
      return BackupEntryFragment()
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
  ): View = BackupEntryFragmentBinding.inflate(layoutInflater).root

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
      walletsEventSender.sendBackupInfoEvent(
        WalletsAnalytics.ACTION_NEXT,
        if (password.isNotEmpty()) WalletsAnalytics.PASSWORD else WalletsAnalytics.NO_PASSWORD,
      )
      navigator.showBackupCreationScreen(
        requireArguments().getString(WALLET_ADDRESS_KEY)!!, password
      )
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
    val passwordTextWatcher = object : TextWatcher {
      var timer: CountDownTimer? = null

      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
      override fun afterTextChanged(s: Editable) {
        timer?.cancel() // wait until the user stops writing for 0.5s to validate password fields
        timer = object : CountDownTimer(500, 500) {
          override fun onTick(millisUntilFinished: Long) {}
          override fun onFinish() {
            handlePasswordFields()
          }
        }.start()
      }
    }

    views.passwordToggle?.backupPasswordInput?.addTextChangedListener(passwordTextWatcher)
    views.passwordToggle?.backupRepeatPasswordInput?.addTextChangedListener(passwordTextWatcher)
  }

  private fun handlePasswordFields() {
    val password = views.passwordToggle?.backupPasswordInput?.getText()
    val repeatedPassword = views.passwordToggle?.backupRepeatPasswordInput?.getText()

    if (views.passwordToggle?.passwordGroup?.isVisible == false) {
      showPasswordError(false)
      views.backupBtn.isEnabled = true
    } else if (password!!.isEmpty() || repeatedPassword!!.isEmpty()) {
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

  override fun onStateChanged(state: BackupEntryState) {
    views.walletBackupInfo.backupWalletAddress.text = state.walletAddress
    handleBalanceAsync(state.balanceAsync)
  }

  private fun handleBalanceAsync(balanceAsync: Async<Balance>) {
    when (balanceAsync) {
      is Async.Success -> {
        setBalance(balanceAsync())
      }
      else -> Unit
    }
  }

  private fun setBalance(balance: Balance) {
    views.walletBackupInfo.backupBalance.text =
      getString(R.string.value_fiat, balance.symbol, balance.amount)
  }

  override fun onSideEffect(sideEffect: BackupEntrySideEffect) = Unit
}
