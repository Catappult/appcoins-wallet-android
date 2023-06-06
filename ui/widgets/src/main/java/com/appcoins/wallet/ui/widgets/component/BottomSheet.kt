package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletBottomSheet(
  openBottomSheet: Boolean,
  onDismissRequest: () -> Unit,
  bottomSheetState: SheetState = rememberModalBottomSheetState(false),
  content: @Composable ColumnScope.() -> Unit
) {
  if (openBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = onDismissRequest,
      sheetState = bottomSheetState,
      containerColor = WalletColors.styleguide_blue_secondary
    ) {
      Column(
        modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .padding(bottom = 32.dp, top = 8.dp)
          .imePadding(),
      ) {
        content()
      }
    }
  }
}

