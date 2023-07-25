package com.asfoundation.wallet.wallet.home.app_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.appcoins.wallet.ui.widgets.R
import com.asfoundation.wallet.viewmodel.AppDetailsViewModel
import javax.inject.Inject

class AppViewFragment(val gamePackage: String) : DialogFragment() {


    private val viewModel: AppDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    AppViewScreen(appDetailsData = viewModel.gameDetails.value) {
                        viewModel.fetchGameDetails(gamePackage)
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    @Composable
    fun AppViewScreen(appDetailsData: GameDetailsData, function: () -> Unit) {
        function()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Image(
                painter = painterResource(id = R.drawable.ic_eskills),
                contentDescription = stringResource(id = R.string.e_skills_know_more_about),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.weight(0.3f))
            Text(
                text = appDetailsData.description,
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}