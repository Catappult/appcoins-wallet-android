package com.asfoundation.wallet.main.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.jvm_common.RxBus
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asf.wallet.databinding.SplashExtenderFragmentBinding
import com.asfoundation.wallet.main.splash.bus.SplashFinishEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashExtenderFragment :
    Fragment(), SingleStateFragment<SplashExtenderState, SplashExtenderSideEffect> {

  private val viewModel: SplashExtenderViewModel by viewModels()
  private val views by viewBinding(SplashExtenderFragmentBinding::bind)

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View = SplashExtenderFragmentBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    views.composeView.setContent {
      VipWelcomeScreen(
          onClick = {
            viewModel.completeVipOnboarding()
            finishSplash()
          })
    }
  }

  override fun onStateChanged(state: SplashExtenderState) = Unit

  override fun onSideEffect(sideEffect: SplashExtenderSideEffect) {
    when (sideEffect) {
      is SplashExtenderSideEffect.ShowVipAnimation -> {
        showVipOnboarding(sideEffect.isVip, sideEffect.showVipOnboarding)
      }
    }
  }

  private fun showVipOnboarding(isVip: Boolean, showVipOnboarding: Boolean) {
    if (isVip) views.splashLogo.setImageResource(R.drawable.ic_vip_symbol)
    if (showVipOnboarding) views.composeView.visibility = View.VISIBLE else finishSplash()
  }

  private fun finishSplash() {
    RxBus.publish(SplashFinishEvent())
  }

  @Preview
  @Composable
  fun VipWelcomeScreen(onClick: () -> Unit = {}) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .background(color = WalletColors.styleguide_blue)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Image(
              painter = painterResource(R.drawable.img_vip_onboarding),
              contentDescription = null,
              modifier = Modifier.height(400.dp).widthIn(max = 400.dp).fillMaxWidth())
          Image(
              painter = painterResource(R.drawable.ic_vip_symbol),
              contentDescription = null,
              modifier = Modifier.size(80.dp).padding(horizontal = 8.dp))
          Text(
              text = stringResource(R.string.vip_program_onboarding_header_1),
              modifier = Modifier.padding(top = 32.dp),
              style = MaterialTheme.typography.headlineLarge,
              color = WalletColors.styleguide_light_grey,
              fontWeight = FontWeight.Bold)
          Text(
              text = stringResource(R.string.vip_program_onboarding_header_2),
              modifier = Modifier.padding(vertical = 8.dp),
              style = MaterialTheme.typography.titleLarge,
              color = WalletColors.styleguide_vip_yellow,
          )
          Text(
              text = stringResource(R.string.vip_program_onboarding_body),
              modifier =
                  Modifier.padding(horizontal = 24.dp, vertical = 8.dp).widthIn(max = 400.dp),
              style = MaterialTheme.typography.bodySmall,
              color = WalletColors.styleguide_light_grey,
              textAlign = TextAlign.Center,
          )
          Column(
              modifier =
                  Modifier.padding(vertical = 48.dp, horizontal = 32.dp).widthIn(max = 360.dp)) {
                ButtonWithText(
                    label = stringResource(R.string.got_it_button),
                    onClick = onClick,
                    labelColor = WalletColors.styleguide_blue,
                    backgroundColor = WalletColors.styleguide_vip_yellow,
                    buttonType = ButtonType.LARGE)
              }
        }
  }
}
