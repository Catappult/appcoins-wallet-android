package com.asfoundation.wallet.di;

import com.asfoundation.wallet.ui.SettingsFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module public interface SettingsModule {
  @FragmentScope @ContributesAndroidInjector() SettingsFragment settingsFragment();
}
