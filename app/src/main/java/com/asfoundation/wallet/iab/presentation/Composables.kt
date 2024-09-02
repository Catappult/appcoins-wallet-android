package com.asfoundation.wallet.iab.presentation

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
  name = "Min scale all",
  group = "min scale all",
  fontScale = 0.85f,
  device = "id:5.4in FWVGA",
  showSystemUi = true
)
@Preview(
  name = "Normal scale",
  group = "normal scale",
  fontScale = 1.0f,
  device = "id:pixel_5",
  showSystemUi = true
)
@Preview(
  name = "Max scale all",
  group = "max scale all",
  fontScale = 1.3f,
  device = "id:3.7 FWVGA slider",
  showSystemUi = true
)
@Preview(
  name = "Max scale font",
  group = "max scale font",
  fontScale = 1.3f,
  device = "id:pixel_5",
  showSystemUi = true
)
@Preview(
  name = "Max scale screen",
  group = "max scale screen",
  fontScale = 1.0f,
  device = "id:3.7 FWVGA slider",
  showSystemUi = true
)
annotation class PreviewLight

@Preview(
  name = "Min scale all dark",
  group = "min scale all",
  fontScale = 0.85f,
  device = "id:5.4in FWVGA",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Normal scale dark",
  group = "normal scale",
  fontScale = 1.0f,
  device = "id:pixel_5",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale all dark",
  group = "max scale all",
  fontScale = 1.3f,
  device = "id:3.7 FWVGA slider",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale font dark",
  group = "max scale font",
  fontScale = 1.3f,
  device = "id:pixel_5",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale screen dark",
  group = "max scale screen",
  fontScale = 1.0f,
  device = "id:3.7 FWVGA slider",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class PreviewDark

@PreviewLight
@PreviewDark
annotation class PreviewAllPortrait

@Preview(
  name = "Min scale all",
  group = "min scale all",
  fontScale = 0.85f,
  device = "spec:parent=5.4in FWVGA,orientation=landscape",
  showSystemUi = true
)
@Preview(
  name = "Normal scale",
  group = "normal scale",
  fontScale = 1.0f,
  device = "spec:parent=pixel_5,orientation=landscape",
  showSystemUi = true
)
@Preview(
  name = "Max scale all",
  group = "max scale all",
  fontScale = 1.3f,
  device = "spec:parent=3.7 FWVGA slider,orientation=landscape",
  showSystemUi = true
)
@Preview(
  name = "Max scale font",
  group = "max scale font",
  fontScale = 1.3f,
  device = "spec:parent=pixel_5,orientation=landscape",
  showSystemUi = true
)
@Preview(
  name = "Max scale screen",
  group = "max scale screen",
  fontScale = 1.0f,
  device = "spec:parent=3.7 FWVGA slider,orientation=landscape",
  showSystemUi = true
)
annotation class PreviewLandscapeLight

@Preview(
  name = "Min scale all dark",
  group = "min scale all",
  fontScale = 0.85f,
  device = "spec:parent=5.4in FWVGA,orientation=landscape",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Normal scale dark",
  group = "normal scale",
  fontScale = 1.0f,
  device = "spec:parent=pixel_5,orientation=landscape",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale all dark",
  group = "max scale all",
  fontScale = 1.3f,
  device = "spec:parent=3.7 FWVGA slider,orientation=landscape",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale font dark",
  group = "max scale font",
  fontScale = 1.3f,
  device = "spec:parent=pixel_5,orientation=landscape",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
  name = "Max scale screen dark",
  group = "max scale screen",
  fontScale = 1.0f,
  device = "spec:parent=3.7 FWVGA slider,orientation=landscape",
  showSystemUi = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class PreviewLandscapeDark

@PreviewLandscapeLight
@PreviewLandscapeDark
annotation class PreviewAllLandscape

@PreviewAllLandscape
@PreviewAllPortrait
annotation class PreviewAll
