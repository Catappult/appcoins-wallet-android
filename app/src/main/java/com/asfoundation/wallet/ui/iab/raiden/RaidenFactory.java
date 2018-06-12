package com.asfoundation.wallet.ui.iab.raiden;

import com.asf.microraidenj.MicroRaidenClient;
import com.asf.microraidenj.contract.MicroRaidenContract;
import com.asf.microraidenj.eth.ChannelBlockObtainer;
import com.asf.microraidenj.type.Address;
import com.asfoundation.wallet.repository.GasSettingsRepositoryType;
import com.asfoundation.wallet.repository.Web3jProvider;
import com.bds.microraidenj.DefaultChannelBlockObtainer;
import com.bds.microraidenj.DefaultGasLimitEstimator;
import com.bds.microraidenj.DefaultMicroRaidenBDS;
import com.bds.microraidenj.DefaultMicroRaidenClient;
import com.bds.microraidenj.MicroRaidenBDS;
import com.bds.microraidenj.util.DefaultTransactionSender;
import com.bds.microraidenj.ws.BDSMicroRaidenApi;
import java.math.BigDecimal;
import java.math.BigInteger;

public class RaidenFactory {
  private static final String TAG = RaidenFactory.class.getSimpleName();
  private final Address channelManagerAddr =
      Address.from("0x97a3e71e4d9cb19542574457939a247491152e81");
  private final Address tokenAddr = Address.from("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3");
  private final BigInteger maxDeposit = new BigInteger("1000000000000000000000000");
  private final Web3jProvider web3jProvider;
  private final GasSettingsRepositoryType gasSettingsRepositoryType;
  private final NonceObtainer nonceObtainer;

  public RaidenFactory(Web3jProvider web3jProvider,
      GasSettingsRepositoryType gasSettingsRepositoryType, NonceObtainer nonceObtainer) {
    this.web3jProvider = web3jProvider;
    this.gasSettingsRepositoryType = gasSettingsRepositoryType;
    this.nonceObtainer = nonceObtainer;
  }

  public MicroRaidenBDS get() {
    MicroRaidenContract microRaidenContract = new MicroRaidenContract(channelManagerAddr, tokenAddr,
        new DefaultTransactionSender(web3jProvider.getDefault(),
            () -> gasSettingsRepositoryType.getGasSettings(true)
                .map(gasSettings -> gasSettings.gasPrice.multiply(BigDecimal.valueOf(18))
                    .toBigInteger())
                .blockingGet(), nonceObtainer,
            new DefaultGasLimitEstimator(web3jProvider.getDefault())));
    ChannelBlockObtainer channelBlockObtainer =
        new DefaultChannelBlockObtainer(web3jProvider.getDefault(), 5, 1500);
    MicroRaidenClient microRaidenClient =
        new DefaultMicroRaidenClient(channelManagerAddr, maxDeposit, channelBlockObtainer,
            microRaidenContract);
    BDSMicroRaidenApi bdsMicroRaidenApi = BDSMicroRaidenApi.create(true);
    return new DefaultMicroRaidenBDS(microRaidenClient, bdsMicroRaidenApi);
  }
}
