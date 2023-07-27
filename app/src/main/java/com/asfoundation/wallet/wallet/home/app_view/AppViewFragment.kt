package com.asfoundation.wallet.wallet.home.app_view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.widgets.GameDetails
import com.appcoins.wallet.ui.widgets.R
import com.asfoundation.wallet.viewmodel.AppDetailsViewModel
import com.asfoundation.wallet.wallet.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                    AppViewScreen()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    @Composable
    fun AppViewScreen() {
        GameDetails(
            appDetailsData = viewModel.gameDetails.value,
            close = { closeFragment() }
            ) {
            viewModel.fetchGameDetails(gamePackage)
        }
    }

    fun closeFragment() {
        Log.i("Parent", "Parent Fragment -> "+parentFragment)
        parentFragmentManager.beginTransaction().remove(this).commit();   }

}
@Preview
@Composable
fun PreviewAppView() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        //modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box() {
            AsyncImage(
                model = "https://pool.img.aptoide.com/catappult/ad72b51875828f10222af84ebc55b761_feature_graphic.png",
                contentDescription = "game background"
            )
            AsyncImage(
                model = "https://pool.img.aptoide.com/catappult/57d4a771e6dbedff5e5f8db37687c3dc_icon.png",
                contentDescription = "game icon"
            )

        }
        Image(
            painter = painterResource(id = R.drawable.ic_eskills),
            contentDescription = stringResource(id = R.string.e_skills_know_more_about),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.weight(0.1f))
        Text(
            text = "Descricao Teste",
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.weight(0.2f))
    }
}
