package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.appcoins.wallet.ui.common.theme.WalletColors


@Preview
@Composable
private fun CardActivePromoCodeExample() {
  ActivePromoCodeComposable(cardItem = promoCodeItem)
}

@Composable
fun ActivePromoCodeComposable(cardItem: ActiveCardPromoCodeItem) {
  Column(modifier = Modifier
    .padding(
      start = 16.dp,
      end = 16.dp,
      top = 18.dp
    )) {
    Box(
      modifier = Modifier
        .align(Alignment.End)
        .clip(RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp))
        .background(WalletColors.styleguide_pink)
    ) {
      Text(
        text = stringResource(id = R.string.promo_code_settings_title),
        fontSize = 12.sp,
        color = WalletColors.styleguide_light_grey,
        modifier = Modifier.padding(
          top = 6.dp,
          end = 14.dp,
          start = 14.dp,
          bottom = 6.dp
        )
      )
    }
    Surface(
      color = WalletColors.styleguide_blue_secondary,
      modifier = Modifier
        .border(
          border = BorderStroke(2.dp, WalletColors.styleguide_pink),
          shape = RoundedCornerShape(
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
            topStart = 16.dp
          )
        )
        .clip(
          shape = RoundedCornerShape(
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
            topStart = 16.dp
          )
        )
        .zIndex(4f)
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        ImageWithTitleAndDescription(
          cardItem.imageUrl,
          cardItem.title,
          cardItem.subtitle,
          true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Column(
          modifier = Modifier
            .height(32.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(start = 8.dp)
            ) {
              Text(
                text = stringResource(R.string.rewards_promo_code_status),
                fontWeight = FontWeight.Bold,
                color = WalletColors.styleguide_dark_grey,
                maxLines = 1,
                fontSize = 14.sp
              )
              Text(
                text = stringResource(R.string.rewards_promo_code_status_active),
                fontWeight = FontWeight.Bold,
                color = WalletColors.styleguide_green,
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
                fontSize = 14.sp
              )
            }
            GetText(cardItem.action, cardItem.packageName)
          }
        }
      }
    }
  }
}


//Test Itens
data class ActiveCardPromoCodeItem(
  val title: String?,
  val subtitle: String?,
  val imageUrl: String?,
  val urlRedirect: String?,
  val packageName: String?,
  val status: Boolean,
  val action: () -> Unit
)

val promoCodeItem = ActiveCardPromoCodeItem(
  title = "Days of empire",
  subtitle = "Receive an extra 15% Bonus in all your purchases.",
  imageUrl = "https://img.freepik.com/vetores-gratis/astronauta-bonito-relaxamento-frio-na-ilustracao-do-icone-do-vetor-dos-desenhos-animados-do-controlador-de-jogo-conceito-de-icone-de-ciencia-de-tecnologia-isolado-vetor-premium-estilo-flat-cartoon_138676-3717.jpg?w=2000",
  urlRedirect = "https://example.com",
  packageName = null,
  status  = true,
  action = { /* handle click action */ }
)
