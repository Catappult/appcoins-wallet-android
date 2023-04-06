package com.appcoins.wallet.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.widgets.TopBar
import com.asfoundation.wallet.viewmodel.BasePageViewFragment

class HomeFragment_new : BasePageViewFragment(),
  SingleStateFragment<HomeState, HomeSideEffect> {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        HomeScreen()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onStateChanged(state: HomeState) {
  }

  override fun onSideEffect(sideEffect: HomeSideEffect) {
  }

  @Composable
  fun HomeScreen(
    modifier: Modifier = Modifier,
  ) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
      scaffoldState = scaffoldState,
      topBar = {
        Surface(elevation = 4.dp) {
          TopBar(
            isMainBar = true,
            isVip = false,
            onClickNotifications = { Log.d("TestHomeFragment", "Notifications")},
            onClickSettings = { Log.d("TestHomeFragment", "Settings")},
            onClickSupport = { Log.d("TestHomeFragment", "Support")},
          )
        }
      },
      backgroundColor = colorResource(R.color.styleguide_blue),
      modifier = modifier
    ) { padding ->
      HomeScreenContent(
        padding = padding
      )
    }
  }

  @Composable
  internal fun HomeScreenContent(
    padding: PaddingValues
  ) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      DummyCard()
      DummyCard()
      DummyCard()
      DummyCard()
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
      backgroundColor = colorResource(R.color.styleguide_blue_secondary),
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Home Screen",
          style = MaterialTheme.typography.h5,
          color = colorResource(R.color.styleguide_white)
        )
      }
    }
  }

  @Preview(showBackground = true)
  @Composable
  fun HomeScreenPreview() {
    HomeScreen()
  }

}
