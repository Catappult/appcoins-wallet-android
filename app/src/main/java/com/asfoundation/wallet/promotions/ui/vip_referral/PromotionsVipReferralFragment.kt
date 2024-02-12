package com.asfoundation.wallet.promotions.ui.vip_referral

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.VipReferralEndCountDownTimer
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.promotions.ui.vip_referral.PromotionsVipReferralViewModel.UiState
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromotionsVipReferralFragment : BasePageViewFragment() {

  @Inject lateinit var navigator: PromotionsVipReferralNavigator

  private val viewModel: PromotionsVipReferralViewModel by viewModels()

  @Inject lateinit var formatter: CurrencyFormatUtils

  private lateinit var promoReferral: String
  private lateinit var earnedValue: String
  private lateinit var earnedTotal: String
  private lateinit var bonusPercent: String
  private lateinit var appName: String
  private lateinit var appIconUrl: String
  private var endDate: Long = 0L

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { VipReferralProgramScreen() } }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.getCurrency(earnedValue)
  }

  @Composable
  fun VipReferralProgramScreen() {
    Scaffold(
        topBar = { Surface { TopBar(onClickSupport = { viewModel.displayChat() }) } },
        containerColor = WalletColors.styleguide_blue,
    ) { padding ->
      Column(
          modifier =
              Modifier.padding(padding)
                  .padding(16.dp)
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        VipReferralProgramContent(uiState = viewModel.uiState.collectAsState().value)
      }
    }
  }

  @Composable
  fun VipReferralProgramContent(uiState: UiState) {
    when (uiState) {
      is UiState.Success -> {
        val fiatAmount = formatter.formatCurrency(uiState.fiatValue!!.amount, WalletCurrency.FIAT)
        VipPresentation(bonusPercent)
        VipCard(
            symbol = uiState.fiatValue.symbol,
            fiatAmount = fiatAmount,
            earnedTotal = earnedTotal,
            referralCode = promoReferral,
            endDate = endDate,
            appName = appName,
            appIconUrl = appIconUrl)
      }
      else -> Loading()
    }
  }

  @Composable
  fun Loading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          CircularProgressIndicator()
        }
  }

  @Composable
  fun VipPresentation(bonusPercent: String) {
    Column(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Image(
              painter = painterResource(R.drawable.img_vip_referral),
              contentDescription = null,
              modifier = Modifier.size(240.dp).padding(bottom = 16.dp).fillMaxWidth())
          Text(
              text = stringResource(R.string.vip_program_referral_page_title),
              modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, end = 8.dp).fillMaxWidth(),
              style = MaterialTheme.typography.headlineMedium,
              color = WalletColors.styleguide_light_grey,
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold)
          Text(
              text = stringResource(R.string.vip_program_referral_page_body_2, bonusPercent),
              modifier = Modifier.padding(start = 8.dp, bottom = 16.dp, end = 8.dp).fillMaxWidth(),
              style = MaterialTheme.typography.bodyMedium,
              color = WalletColors.styleguide_light_grey)
        }
  }

  @Composable
  fun VipCard(
      symbol: String,
      fiatAmount: String,
      earnedTotal: String,
      referralCode: String,
      endDate: Long,
      appName: String,
      appIconUrl: String,
  ) {
    Card(
        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary)) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            VipReferralEndCountDownTimer(
                endDateTime = endDate,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth())
            ShareCodeCard(referralCode, appName, appIconUrl)
            EarnedCreditsInfo(symbol = symbol, fiatAmount = fiatAmount, earnedTotal = earnedTotal)
          }
        }
  }

  @Composable
  fun ShareCodeCard(referralCode: String, appName: String, appIconUrl: String) {
    Text(
        text = stringResource(R.string.vip_program_referral_page_share_title),
        modifier = Modifier.padding(8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = WalletColors.styleguide_medium_grey,
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = WalletColors.styleguide_grey_blue_background)) {
          Column(modifier = Modifier.padding(16.dp)) {
            GameHeaderCard(appName, appIconUrl)
            ShareCodeCardButton(referralCode = referralCode, onClick = { shareCode() })
          }
        }
  }

  @Composable
  fun GameHeaderCard(appName: String, appIconUrl: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      SubcomposeAsyncImage(
          model = appIconUrl,
          contentDescription = null,
          contentScale = ContentScale.Fit,
          modifier = Modifier.size(32.dp).clip(shape = RoundedCornerShape(8.dp)),
          loading = { CircularProgressIndicator() },
          error = {
            Image(
                painter = painterResource(R.drawable.ic_transaction_fallback),
                contentDescription = null,
                modifier = Modifier.size(32.dp))
          },
      )
      Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(start = 16.dp)) {
            Text(
                stringResource(R.string.vip_program_referral_page_only_app_title),
                fontSize = 11.sp,
                color = WalletColors.styleguide_dark_grey,
            )
            Text(
                text = appName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = WalletColors.styleguide_light_grey)
          }
    }
  }

  @Composable
  fun ShareCodeCardButton(referralCode: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary),
        shape =
            RoundedCornerShape(
                bottomStart = 16.dp, topStart = 16.dp, bottomEnd = 32.dp, topEnd = 32.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = referralCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WalletColors.styleguide_light_grey)
                ButtonWithText(
                    label = stringResource(R.string.wallet_view_share_button),
                    onClick = onClick,
                    labelColor = WalletColors.styleguide_blue,
                    backgroundColor = WalletColors.styleguide_vip_yellow,
                )
              }
        }
  }

  @Composable
  fun EarnedCreditsInfo(symbol: String, fiatAmount: String, earnedTotal: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
          Image(
              painter = painterResource(R.drawable.ic_vip_ref_coins),
              contentDescription = null,
              modifier = Modifier.height(32.dp).padding(horizontal = 8.dp))
          Text(
              text =
                  stringResource(
                      R.string.vip_program_referral_page_earned_body,
                      symbol,
                      fiatAmount,
                      earnedTotal),
              color = WalletColors.styleguide_light_grey,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold)
        }
  }

  @Preview
  @Composable
  fun PreviewVipPresentation() {
    VipPresentation(bonusPercent = "20")
  }

  @Preview
  @Composable
  fun PreviewVipCard() {
    VipCard(
        symbol = "$",
        fiatAmount = "100",
        earnedTotal = "1000",
        "GH1TLF3456",
        1234567L,
        "Trivial Drive app",
        "https://www.img.com")
  }

  private fun setupView() =
      requireArguments().run {
        bonusPercent = getString(BONUS_PERCENT) ?: ""
        endDate = getLong(END_DATE)
        promoReferral = getString(PROMO_REFERRAL) ?: ""
        earnedValue = getString(EARNED_VALUE) ?: ""
        earnedTotal = getString(EARNED_TOTAL) ?: ""
        appName = getString(APP_NAME) ?: ""
        appIconUrl = getString(APP_ICON_URL) ?: ""
      }

  private fun shareCode() {
    ShareCompat.IntentBuilder(requireActivity())
        .setText(promoReferral)
        .setType("text/*")
        .startChooser()
  }

  companion object {
    internal const val BONUS_PERCENT = "vip_bonus"
    internal const val PROMO_REFERRAL = "vip_code"
    internal const val EARNED_VALUE = "total_earned"
    internal const val EARNED_TOTAL = "number_referrals"
    internal const val END_DATE = "end_date"
    internal const val APP_NAME = "app_name"
    internal const val APP_ICON_URL = "app_icon_url"
  }
}
