package com.asfoundation.wallet.poa;

import io.reactivex.Single;

public interface ProofWriter {
  Single<String> writeProof(Proof proof);

  Single<ProofSubmissionFeeData> hasWalletPrepared(int chainId);
}
