package com.asfoundation.wallet.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.arch.SingleStateFragment
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.viewmodel.BasePageViewFragment

//TODO to be removed. for testing
class TestHomeFragment : BasePageViewFragment(),
  SingleStateFragment<HomeState, HomeSideEffect> {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = colorResource(R.color.styleguide_blue)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
          ) {
            TopBar(
              isMainBar = true,
              isVip = false
            )
            Card(
              modifier = Modifier
                .padding(
                  start = 16.dp,
                  end = 16.dp,
                  top = 16.dp
                ),
              shape = RoundedCornerShape(8.dp),
              backgroundColor = colorResource(R.color.styleguide_blue_secondary),
            ) {
              Column(
                modifier = Modifier
                  .height(200.dp)
                  .padding(16.dp)
                  .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
              ){
                Text(
                  text = "Home Screen",
                  style = MaterialTheme.typography.h5,
                  color = colorResource(R.color.styleguide_white)
                )
              }
            }
          }
        }
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

}
