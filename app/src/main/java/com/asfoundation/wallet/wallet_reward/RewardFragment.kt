package com.asfoundation.wallet.wallet_reward

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_orange
import com.appcoins.wallet.ui.widgets.GamificationHeader
import com.appcoins.wallet.ui.widgets.RewardsActions
import com.appcoins.wallet.ui.widgets.TopBar
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RewardFragment : BasePageViewFragment(), SingleStateFragment<RewardState, RewardSideEffect> {

    @Inject
    lateinit var navigator: RewardNavigator
    private val viewModel: RewardViewModel by viewModels()

    private var isVip by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RewardScreen()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RewardScreen(
        modifier: Modifier = Modifier,
    ) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 4.dp) {
                    TopBar(
                        isMainBar = true,
                        isVip = isVip,
                        onClickNotifications = { Log.d("TestHomeFragment", "Notifications") },
                        onClickSettings = { viewModel.onSettingsClick() },
                        onClickSupport = { viewModel.showSupportScreen(false) },
                    )
                }
            },
            containerColor = WalletColors.styleguide_blue,
            modifier = modifier
        ) { padding ->
            RewardScreenContent(
                padding = padding
            )
        }
    }

    @Composable
    internal fun RewardScreenContent(
        padding: PaddingValues
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding),
        ) {
            GamificationHeader(
                { /*TODO navigate to gamification */ },
                indicatorColor = styleguide_orange, // TODO
                currentProgress = 8000,  //TODO
                maxProgress = 15000  //TODO
            )
            RewardsActions(
                { navigator.navigateToWithdrawScreen() },
                { navigator.showPromoCodeFragment() },
                { navigator.showGiftCardFragment() }
            )
            DummyCard()
        }
    }

    @Composable
    fun DummyCard() {
        Card(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reward Screen",
                    style = MaterialTheme.typography.titleMedium,
                    color = WalletColors.styleguide_white
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun RewardScreenPreview() {
        RewardScreen()
    }

    override fun onStateChanged(state: RewardState) {
        showVipBadge(state.showVipBadge)
    }

    override fun onSideEffect(sideEffect: RewardSideEffect) {
        when (sideEffect) {
            is RewardSideEffect.NavigateToSettings -> navigator.navigateToSettings(
                sideEffect.turnOnFingerprint
            )
        }
    }

    private fun showVipBadge(shouldShow: Boolean) {
        isVip = shouldShow
    }

}
