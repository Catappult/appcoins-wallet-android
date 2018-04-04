package com.asfoundation.wallet.advertise;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Joao Raimundo on 28/03/2018.
 */

public interface ServiceConnector {
  /** WARNING: The values for the messages are used on both, sdk and wallet side. So when a new is
   * message value is added on any side please replicate that change of the interface that is
   * missing it.
   */
  /**
   * Command to the service to register the Ad campaign
   */
  int MSG_REGISTER_CAMPAIGN = 1;
  /**
   * Command to the service to send a proof of attention (PoA)
   */
  int MSG_SEND_PROOF = 2;
  /**
   * Actions used to communicate with the service on the wallet side, for the bind.
   */
  String ACTION_BIND = "com.asf.appcoins.service.ACTION_BIND";
  /**
   * Actions used to listen to the confirmation of the handshake.
   */
  String ACTION_ACK_BROADCAST = "com.asf.appcoins.service.ACTION_ACK_BROADCAST";
  /**
   * Intent parameter for the wallet package name, that was obtained on the handshake.
   */
  String PARAM_WALLET_PACKAGE_NAME = "PARAM_WALLET_PKG_NAME";
  /**
   * Intent parameter for the application package name, to be used ont the second step of the
   * handshake.
   */
  String PARAM_APP_PACKAGE_NAME = "PARAM_APP_PKG_NAME";
  /**
   * Intent parameter for the application service name, to be used ont the second step of the
   * handshake.
   */
  String PARAM_APP_SERVICE_NAME = "PARAM_APP_SERVICE_NAME";

  /**
   * Method to bind to the service on the received package name and listening to the for the given
   * action of the intent filter.
   *
   * @param context     The context where this connector is being used.
   * @retun true if the bind was successful, false otherwise.
   */
  boolean connectToService(Context context);

  /**
   * Method to unbind with the service that handles the PoA process.
   *
   * @param context     The context where this connector is being used.
   */
  void disconnectFromService(Context context);

  /**
   * Method to send the message to the bound service.
   *
   * @param context     The context where this connector is being used.
   * @param messageType The type of message being send. Possible values at the moment:
   *                   {MSG_REGISTER_CAMPAIGN, MSG_SEND_PROOF and MSG_SIGN_PROOF}.
   * @param content     The package name where the service is located.
   */
  void sendMessage(Context context, int messageType, Bundle content);

}
