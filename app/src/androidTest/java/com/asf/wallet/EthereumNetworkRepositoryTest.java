package com.asf.wallet;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.asfoundation.wallet.repository.EthereumNetworkRepository;
import com.asfoundation.wallet.repository.EthereumNetworkRepositoryType;
import com.asfoundation.wallet.repository.PreferenceRepositoryType;
import com.asfoundation.wallet.repository.SharedPreferenceRepository;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class) public class EthereumNetworkRepositoryTest {

  private EthereumNetworkRepositoryType networkRepository;

  @Before public void setUp() {
    Context context = InstrumentationRegistry.getTargetContext();
    PreferenceRepositoryType preferenceRepositoryType = new SharedPreferenceRepository(context);
    networkRepository = new EthereumNetworkRepository(preferenceRepositoryType);
  }
}
