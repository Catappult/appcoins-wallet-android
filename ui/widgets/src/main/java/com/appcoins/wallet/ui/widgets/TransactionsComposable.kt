package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_green
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_primary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
  icon: Int?,
  appIcon: String?,
  title: String,
  description: String?,
  amount: String?,
  subIcon: Int?,
  onClick: () -> Unit,
  textDecoration: TextDecoration,
  isPending: Boolean,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = styleguide_dark_secondary),
    onClick = onClick
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier =
      Modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 64.dp)
        .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TransactionIcon(icon, appIcon, subIcon)
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.fillMaxWidth(0.6f)) {
            Text(
              text = title,
              fontWeight = FontWeight.Bold,
              color = styleguide_light_grey,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (description != null)
              Text(
                text = description,
                color = styleguide_dark_grey,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
          }
          Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.9f)
          ) {
            if (amount != null)
              Box {
                Text(
                  text = amount,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium,
                  textAlign = TextAlign.End,
                  color = if (isPending)
                    styleguide_dark_grey
                  else
                    styleguide_light_grey,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (textDecoration == TextDecoration.LineThrough) {
                  Canvas(modifier = Modifier.matchParentSize()) {
                    val textHeight = size.height / 1.90f
                    drawLine(
                      color = styleguide_dark_grey,
                      start = Offset(0f, textHeight),
                      end = Offset(size.width, textHeight),
                      strokeWidth = 1.5.dp.toPx()
                    )
                  }
                }
              }
          }
        }
      }
    }
  }
}

@Composable
fun TransactionSeparator(text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
  ) {
    Text(
      text = text,
      color = styleguide_dark_grey,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
fun TransactionIcon(
  icon: Int? = null,
  appIcon: String? = null,
  subIcon: Int? = null,
  imageSize: Dp = 40.dp
) {
  Box(contentAlignment = Alignment.BottomEnd) {
    if (icon != null)
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(imageSize)
      )
    else if (appIcon != null)
      SubcomposeAsyncImage(
        model = appIcon,
        contentDescription = null,
        modifier = Modifier
          .size(imageSize)
          .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
        loading = { CircularProgressIndicator() })
    else
      Icon(
        painter = painterResource(id = R.drawable.ic_transaction_fallback),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(imageSize)
      )

    if (subIcon != null)
      Icon(
        painter = painterResource(subIcon),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(24.dp)
      )
  }
}

@Composable
fun TransactionDetailHeader(
  icon: Int?,
  appIcon: String?,
  amount: String?,
  subIcon: Int?,
  name: String?,
  textDecoration: TextDecoration,
  description: String?
) {
  Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
    Row(
      modifier = Modifier
        .padding(bottom = 16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth(0.8f)) {
        if (amount != null)
          Box {
            Text(
              text = amount,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.headlineSmall,
              textAlign = TextAlign.End,
              color = styleguide_light_grey,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (textDecoration == TextDecoration.LineThrough) {
              Canvas(modifier = Modifier.matchParentSize()) {
                val textHeight = size.height / 1.8f
                drawLine(
                  color = styleguide_red,
                  start = Offset(0f, textHeight),
                  end = Offset(size.width, textHeight),
                  strokeWidth = 1.5.dp.toPx()
                )
              }
            }
          }
        if (name != null)
          Text(
            text = name,
            color = styleguide_light_grey,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
      }
      TransactionIcon(icon, appIcon, subIcon, 56.dp)
    }

    if (description != null && appIcon != null)
      TransactionDetailLinkedHeader(description = description, appIcon = appIcon)
  }
}

@Composable
fun TransactionDetailLinkedHeader(description: String, appIcon: String? = null) {
  Card(colors = CardDefaults.cardColors(styleguide_dark), modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TransactionIcon(appIcon = appIcon, imageSize = 32.dp)
      Text(
        text = description,
        color = styleguide_light_grey,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }
  }
}

@Composable
fun PendingTransactionCard() {

  val composition by
  rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.wait_trasaction))
  val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)

  Card(
    colors = CardDefaults.cardColors(styleguide_dark),
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
          .size(54.dp)
          .scale(1.3333f)
          .padding(bottom = 13.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = buildAnnotatedString {
          withStyle(style = SpanStyle(color = styleguide_light_grey)) {
            append(stringResource(id = R.string.transaction_status_pending) + " - ")
          }
          withStyle(style = SpanStyle(color = styleguide_dark_grey)) {
            append(stringResource(id = R.string.purchase_bank_transfer_pending_disclaimer))
          }
        },
        style = MaterialTheme.typography.bodySmall,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}


@Composable
fun TransactionDetailItem(
  label: String,
  data: String = "",
  dataColor: Color = styleguide_light_grey,
  allowCopy: Boolean = false,
  showDownloadInvoice: Boolean = false,
  onClick: () -> Unit = {}
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = label,
      color = styleguide_dark_grey,
      style = MaterialTheme.typography.bodySmall,
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(start = 48.dp)
    ) {
      if (showDownloadInvoice)
        IconTextButton(
          text = stringResource(id = R.string.download_button),
          R.drawable.ic_download,
          onClick
        )
      else TransactionDetailInfo(data, dataColor, allowCopy, onClick)
    }
  }
}

@Composable
fun TransactionDetailInfo(
  data: String,
  dataColor: Color = styleguide_light_grey,
  allowCopy: Boolean = false,
  onClick: () -> Unit = {}
) {
  if (allowCopy) {
    IconButton(onClick = onClick) {
      Icon(
        painter = painterResource(R.drawable.ic_copy),
        contentDescription = stringResource(R.string.copy),
        tint = styleguide_primary,
        modifier = Modifier.size(14.dp)
      )
    }
  }
  Text(
    text = data,
    color = dataColor,
    style = MaterialTheme.typography.bodySmall,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier
      .widthIn(0.dp, 160.dp)
      .padding(vertical = if (allowCopy) 0.dp else 16.dp)
  )
}

@Composable
fun IconTextButton(text: String, iconId: Int, onClick: () -> Unit = {}) {
  TextButton(onClick = onClick) {
    Icon(
      painter = painterResource(iconId),
      contentDescription = text,
      tint = styleguide_primary,
      modifier = Modifier
        .padding(end = 8.dp)
        .size(14.dp)
    )
    Text(
      text = text,
      color = styleguide_primary,
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.widthIn(0.dp, 160.dp),
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun SkeletonLoadingTransactionCard() {
  Card(
    colors = CardDefaults.cardColors(styleguide_dark_secondary),
    modifier =
    Modifier
      .fillMaxWidth()
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(
          modifier = Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = shimmerSkeleton()),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Spacer(
            modifier = Modifier
              .width(width = 170.dp)
              .height(height = 22.dp)
              .padding(start = 8.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier = Modifier
              .width(width = 90.dp)
              .height(height = 22.dp)
              .padding(end = 16.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
      }
    }
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(
          modifier = Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = shimmerSkeleton()),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Spacer(
            modifier = Modifier
              .width(width = 170.dp)
              .height(height = 22.dp)
              .padding(start = 8.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier = Modifier
              .width(width = 90.dp)
              .height(height = 22.dp)
              .padding(end = 16.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
      }
    }
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(
          modifier = Modifier
            .padding(top = 8.dp)
            .width(46.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = shimmerSkeleton()),
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Spacer(
            modifier = Modifier
              .width(width = 170.dp)
              .height(height = 22.dp)
              .padding(start = 8.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
          Spacer(
            modifier = Modifier
              .width(width = 90.dp)
              .height(height = 22.dp)
              .padding(end = 16.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(brush = shimmerSkeleton()),
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewTransactionCardHeader() {
  TransactionDetailHeader(
    icon = null,
    appIcon = null,
    name = "Trivial Drive",
    amount = "-€12,21238745674839837456.73",
    subIcon = R.drawable.ic_transaction_rejected_mini,
    textDecoration = TextDecoration.LineThrough,
    description = "The Bonus of 10% you received on Mar, 14 2022 has been reverted."
  )
}

@Preview
@Composable
fun PreviewTransactionDetailLinkedHeader() {
  TransactionDetailLinkedHeader(
    description = "Reverted Purchase Bonus test used to verify UI",
    appIcon = ""
  )
}

@Preview
@Composable
fun PreviewTransactionDetailItem() {
  TransactionDetailItem(
    label = "Order ID",
    data = "APT453473277845346",
    dataColor = styleguide_green,
    allowCopy = true
  )
}

@Preview
@Composable
fun PreviewTransactionCard() {
  TransactionCard(
    icon = null,
    appIcon = "",
    title = "Reverted Purchase Bonus test used to verify UI",
    description = "AppCoins Trivial demo sample used to test the UI",
    amount = "-€12,21238745674839837456.73",
    subIcon = R.drawable.ic_transaction_rejected_mini,
    {},
    TextDecoration.LineThrough,
    true
  )
}

@Preview
@Composable
fun PreviewTransactionSeparator() {
  TransactionSeparator(text = "Jun, 13 2022")
}

@Preview
@Composable
fun PreviewDownloadButton() {
  IconTextButton(text = "Download", R.drawable.ic_download)
}

@Preview
@Composable
private fun LoadingTransactionCard() {
  SkeletonLoadingTransactionCard()
}

@Preview
@Composable
fun PreviewPendingTransactionCard() {
  PendingTransactionCard()
}
