package com.asfoundation.wallet.poa;

import com.asfoundation.wallet.entity.SubmitPoAException;
import java.net.UnknownHostException;

import static com.asfoundation.wallet.entity.SubmitPoAException.ALREADY_SUBMITTED_FOR_IP;
import static com.asfoundation.wallet.entity.SubmitPoAException.ALREADY_SUBMITTED_FOR_WALLET;
import static com.asfoundation.wallet.entity.SubmitPoAException.CAMPAIGN_NOT_AVAILABLE;
import static com.asfoundation.wallet.entity.SubmitPoAException.CAMPAIGN_NOT_EXISTENT;
import static com.asfoundation.wallet.entity.SubmitPoAException.INCORRECT_DATA;
import static com.asfoundation.wallet.entity.SubmitPoAException.NOT_AVAILABLE_FOR_COUNTRY;
import static com.asfoundation.wallet.entity.SubmitPoAException.NOT_ENOUGH_BUDGET;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.BACKEND_ALREADY_AWARDED;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.BACKEND_CAMPAIGN_NOT_AVAILABLE;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.BACKEND_GENERIC_ERROR;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.BACKEND_INVALID_DATA;
import static com.asfoundation.wallet.poa.BackEndErrorMapper.BackEndError.NO_INTERNET;

public class BackEndErrorMapper {

  public BackEndError map(Throwable throwable) {
    if (throwable instanceof UnknownHostException) {
      return NO_INTERNET;
    }
    if (throwable instanceof SubmitPoAException) {
      switch (((SubmitPoAException) throwable).getError()) {
        case CAMPAIGN_NOT_EXISTENT:
        case CAMPAIGN_NOT_AVAILABLE:
        case NOT_ENOUGH_BUDGET:
          return BACKEND_CAMPAIGN_NOT_AVAILABLE;
        case NOT_AVAILABLE_FOR_COUNTRY:
          return BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY;
        case ALREADY_SUBMITTED_FOR_IP:
        case ALREADY_SUBMITTED_FOR_WALLET:
          return BACKEND_ALREADY_AWARDED;
        case INCORRECT_DATA:
          return BACKEND_INVALID_DATA;
      }
    }
    return BACKEND_GENERIC_ERROR;
  }

  public enum BackEndError {
    BACKEND_GENERIC_ERROR,
    BACKEND_CAMPAIGN_NOT_AVAILABLE,
    BACKEND_CAMPAIGN_NOT_AVAILABLE_ON_COUNTRY,
    BACKEND_ALREADY_AWARDED,
    BACKEND_INVALID_DATA,
    NO_INTERNET,
  }
}
