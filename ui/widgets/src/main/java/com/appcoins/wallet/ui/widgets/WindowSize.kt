package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.unit.dp

const val PORTRAIT_SIZE_LIMIT = 600

fun BoxWithConstraintsScope.expanded() = maxWidth > PORTRAIT_SIZE_LIMIT.dp