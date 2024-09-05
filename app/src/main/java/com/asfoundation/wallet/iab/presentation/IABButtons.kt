package com.asfoundation.wallet.iab.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.asfoundation.wallet.iab.theme.IAPTheme
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IABOpaqueButton(
  modifier: Modifier = Modifier,
  text: String,
  isEnabled: Boolean = true,
  onClick: () -> Unit,
  testTag: String? = null,
) {
  Button(
    onClick = onClick,
    enabled = isEnabled,
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .conditional(
        condition = testTag != null,
        ifTrue = { semantics { testTagsAsResourceId = true }.testTag(testTag!!) }
      ),
    border = BorderStroke(0.dp, Color.Transparent),
    shape = RoundedCornerShape(50),
    colors = ButtonDefaults.buttonColors(
      containerColor = IAPTheme.colors.secondary,
      disabledContentColor = IAPTheme.colors.disabledButton
    ),
  ) {
    Text(
      text = text,
      color = IAPTheme.colors.onSecondary,
      style = IAPTheme.typography.bodyLarge
    )
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IABTransparentButton(
  modifier: Modifier = Modifier,
  text: String,
  isEnabled: Boolean = true,
  onClick: () -> Unit,
  testTag: String? = null,
) {
  val color = IAPTheme.colors.transparentButtonText.takeIf { isEnabled }
    ?: IAPTheme.colors.transparentButtonText.copy(alpha = 0.7f)

  Button(
    onClick = onClick,
    enabled = isEnabled,
    modifier = modifier
      .fillMaxWidth()
      .height(40.dp)
      .conditional(
        condition = testTag != null,
        ifTrue = { semantics { testTagsAsResourceId = true }.testTag(testTag!!) }
      ),
    border = BorderStroke(1.dp, color),
    shape = RoundedCornerShape(50),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color.Transparent,
      disabledContentColor = IAPTheme.colors.disabledButton
    ),
  ) {
    Text(
      text = text,
      color = color,
      style = IAPTheme.typography.bodyLarge,
    )
  }
}

@PreviewAll
@Composable
private fun PreviewButtons(modifier: Modifier = Modifier) {
  val enabled = Random.nextBoolean()
  IAPTheme {
    Column(modifier = Modifier.fillMaxWidth()) {
      IABOpaqueButton(
        text = "Enabled".takeIf { enabled } ?: "Disabled",
        isEnabled = enabled,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(20.dp))
      IABTransparentButton(
        text = "Enabled".takeIf { enabled } ?: "Disabled",
        isEnabled = enabled,
        onClick = {}
      )
    }
  }
}
