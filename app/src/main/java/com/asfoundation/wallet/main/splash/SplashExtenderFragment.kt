package com.asfoundation.wallet.main.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.jvm_common.RxBus
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asf.wallet.databinding.SplashExtenderFragmentBinding
import com.asfoundation.wallet.main.splash.bus.SplashFinishEvent
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashExtenderFragment : BasePageViewFragment() {

  private val viewModel: SplashExtenderViewModel by viewModels()
  private val views by viewBinding(SplashExtenderFragmentBinding::bind)

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View = SplashExtenderFragmentBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.welcomeVipComposeView.setContent {
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is SplashExtenderViewModel.UiState.Success -> {
          if (uiState.showVipOnboarding)
              VipWelcomeScreen(
                  onClick = {
                    viewModel.setOnboardingVipVisualisationState(firstVipOnboarding = true)
                    finishSplash()
                  })
          else if (!uiState.isVip) {
            SplashLogo(isVip = uiState.isVip)
            viewModel.setOnboardingVipVisualisationState(firstVipOnboarding = false)
            finishSplash()
          } else {
            SplashLogo(isVip = uiState.isVip)
            finishSplash()
          }
        }
        is SplashExtenderViewModel.UiState.Loading -> SplashLogo(uiState.isVip)
        is SplashExtenderViewModel.UiState.Fail -> finishSplash()
        else -> {
          // Do nothing
        }
      }
    }
  }

  private fun finishSplash() {
    RxBus.publish(SplashFinishEvent())
  }

  @Composable
  fun SplashLogo(isVip: Boolean = false) {
    val logo = if (isVip) R.drawable.ic_vip_symbol else R.drawable.ic_app_logo_icon
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Image(
              painter = painterResource(logo),
              contentDescription = null,
              modifier = Modifier.size(112.dp))
        }
  }

  @Composable
  fun VipWelcomeScreen(onClick: () -> Unit = {}) {
    Column(
        modifier =
            Modifier.background(color = WalletColors.styleguide_blue)
                .verticalScroll(rememberScrollState())
                .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomCenter) {
              Image(
                  painter = painterResource(R.drawable.img_vip_onboarding),
                  contentDescription = null,
                  modifier =
                      Modifier.padding(bottom = 32.dp)
                          .height(400.dp)
                          .widthIn(max = 400.dp)
                          .fillMaxWidth())
              Image(
                  painter = painterResource(R.drawable.ic_vip_symbol),
                  contentDescription = null,
                  modifier = Modifier.size(88.dp).padding(horizontal = 8.dp))
            }
            Text(
                text = stringResource(R.string.vip_program_onboarding_header_1),
                modifier = Modifier.padding(top = 24.dp),
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
          }
          Column(
              modifier =
                  Modifier.padding(vertical = 48.dp, horizontal = 32.dp)
                      .widthIn(max = 360.dp)
                      .fillMaxSize(),
              verticalArrangement = Arrangement.Bottom) {
                ButtonWithText(
                    label = stringResource(R.string.got_it_button),
                    onClick = onClick,
                    labelColor = WalletColors.styleguide_blue,
                    backgroundColor = WalletColors.styleguide_vip_yellow,
                    buttonType = ButtonType.LARGE)
              }
        }
  }

  @Preview
  @Composable
  fun PreviewVipWelcomeScreen() {
    VipWelcomeScreen()
  }

  @Preview
  @Composable
  fun PreviewSplashLogo() {
    SplashLogo()
  }
}
