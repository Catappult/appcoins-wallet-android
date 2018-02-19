package com.asf.wallet.di;

import com.asf.wallet.ui.SettingsFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module public interface SettingsModule {
  @FragmentScope @ContributesAndroidInjector(modules = { SettingsFragmentModule.class })
  SettingsFragment settingsFragment();
}
