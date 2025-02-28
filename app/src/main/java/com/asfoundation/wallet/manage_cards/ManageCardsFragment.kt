package com.asfoundation.wallet.manage_cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.analytics.analytics.manage_cards.ManageCardsAnalytics
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.widgets.AddNewCardComposable
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManageCardsFragment : BasePageViewFragment() {

  private val viewModel: ManageCardsViewModel by viewModels()

  @Inject
  lateinit var manageCardsNavigator: ManageCardsNavigator

  @Inject
  lateinit var manageCardsAnalytics: ManageCardsAnalytics

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private val manageCardSharedViewModel: ManageCardSharedViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    manageCardsAnalytics.managePaymentCardsImpression()
    return ComposeView(requireContext()).apply {
      setContent { ManageCardsView() }
    }
  }

  @Composable
  fun ManageCardsView() {
    ManageToastWarnings()
    if (viewModel.showBottomSheet.value) {
      ShowDeleteBottomSheet()
    } else {
      viewModel.getCards()
    }
    Scaffold(
      topBar = {
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }, fragmentName = fragmentName, buttonsAnalytics = buttonsAnalytics) }
      },
      containerColor = WalletColors.styleguide_dark,
    ) { padding ->
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is ManageCardsViewModel.UiState.StoredCardsInfo -> ManageCardsContent(
          padding = padding, cardsList = uiState.storedCards
        )

        ManageCardsViewModel.UiState.Loading ->
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Center
          ) {
            Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
          }

        else -> {}
      }
    }
  }

  @Composable
  private fun ManageToastWarnings() {
    val isCardSaved by manageCardSharedViewModel.isCardSaved
    LaunchedEffect(key1 = isCardSaved) {
      if (isCardSaved) {
        manageCardsAnalytics.addedNewCardSuccessEvent()
        viewModel.getCards()
        Toast.makeText(context, R.string.card_added_title, Toast.LENGTH_LONG)
          .show()
        manageCardSharedViewModel.resetCardResult()
      }
    }
    val isCardError by manageCardSharedViewModel.isCardError
    LaunchedEffect(key1 = isCardError) {
      if (isCardError) {
        viewModel.getCards()
        Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG)
          .show()
        manageCardSharedViewModel.resetCardResult()
      }
    }
    val isCardDeleted by viewModel.isCardDeleted
    LaunchedEffect(key1 = isCardDeleted) {
      if (isCardDeleted) {
        manageCardsAnalytics.removeCardSuccessEvent()
        viewModel.getCards()
        Toast.makeText(context, R.string.card_removed, Toast.LENGTH_LONG)
          .show()
        viewModel.isCardDeleted.value = false
      }
    }

  }

  @Composable
  internal fun ManageCardsContent(padding: PaddingValues, cardsList: List<StoredCard>) {
    LazyColumn(
      modifier = Modifier
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      item {
        ScreenTitle()
        AddNewCardComposable(
          paddingTop = 24.dp,
          paddingBottom = 0.dp,
          paddingEnd = 0.dp,
          paddingStart = 0.dp,
          cardHeight = 56.dp,
          imageEndPadding = 16.dp,
          imageSize = 36.dp,
          onClickAction = {
            manageCardsAnalytics.openNewCardDetailsPageEvent()
            manageCardsNavigator.navigateToAddCard()
          },
          addIconDrawable = R.drawable.ic_add_card,
          titleText = stringResource(com.appcoins.wallet.ui.widgets.R.string.manage_cards_add_credit_debit_card_button_),
          backgroundColor = styleguide_dark_secondary,
          textColor = styleguide_light_grey
        )
        if (cardsList.isNotEmpty()) {
          ScreenSubtitle()
        }
      }
      items(cardsList) { card ->
        PaymentCardItem(card)
      }
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.manage_cards_header),
      modifier = Modifier.padding(8.dp),
      style = typography.headlineSmall,
      fontWeight = Bold,
      color = styleguide_light_grey,
    )
  }

  @Composable
  fun ScreenSubtitle() {
    Text(
      text = stringResource(R.string.manage_cards_view_manage_subtitle),
      modifier = Modifier.padding(top = 24.dp, start = 8.dp),
      style = typography.bodyMedium,
      fontWeight = Medium,
      fontSize = 12.sp,
      color = styleguide_light_grey,
    )
  }

  @Composable
  fun PaymentCardItem(storedCard: StoredCard) {
    Card(
      colors = CardDefaults.cardColors(containerColor = styleguide_dark_secondary),
      modifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth()
        .height(56.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Image(
          modifier = Modifier
            .padding(16.dp)
            .align(CenterVertically),
          painter = painterResource(storedCard.cardIcon),
          contentDescription = "Card icon",
        )
        Text(
          text = stringResource(R.string.manage_cards_card_ending_body, storedCard.cardLastNumbers),
          modifier = Modifier
            .padding(8.dp)
            .align(CenterVertically),
          style = typography.bodyMedium,
          fontWeight = Medium,
          color = styleguide_light_grey,
        )
        Spacer(modifier = Modifier.weight(1f))
        Image(
          modifier = Modifier
            .align(CenterVertically)
            .padding(end = 22.dp)
            .clickable {
              viewModel.showBottomSheet(true, storedCard)
            },
          painter = painterResource(R.drawable.ic_delete_card),
          contentDescription = "Delete card",
        )
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun ShowDeleteBottomSheet() {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { viewModel.showBottomSheet(false, null) },
      sheetState = bottomSheetState,
      containerColor = styleguide_dark_secondary
    ) {
      if (viewModel.storedCardClicked.value != null) {
        ManageDeleteCardBottomSheet(
          onCancelClick = { viewModel.showBottomSheet(false, null) },
          onConfirmClick = {
            viewModel.storedCardClicked.value?.recurringReference?.let { viewModel.deleteCard(it) }
            manageCardsAnalytics.removeCardClickEvent()
          },
          storedCard = viewModel.storedCardClicked.value!!,
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics,
        )
      }
    }
  }

  @Preview
  @Composable
  fun PreviewManageCardsContent() {
    ManageCardsContent(
      padding = PaddingValues(0.dp), cardsList = listOf(
        StoredCard("1234", R.drawable.ic_card_brand_visa, null, false),
        StoredCard("5678", R.drawable.ic_card_brand_master_card, null, true)
      )
    )
  }

}
