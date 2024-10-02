package com.asfoundation.wallet.iab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.iab.di.FragmentNavigatorFactory
import javax.inject.Inject

abstract class IabBaseFragment : Fragment() {

  @Inject
  lateinit var fragmentNavigatorFactory: FragmentNavigatorFactory

  val navigator by lazy { fragmentNavigatorFactory.create(findNavController()) }

  @Composable abstract fun FragmentContent()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = ComposeView(requireContext()).apply { setContent { FragmentContent() } }

}
