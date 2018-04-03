package com.asfoundation.wallet.advertise;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Joao Raimundo on 28/03/2018.
 */

public interface ServiceConnector {

    /**
     * Information for the shared preferences used by the Proof of Attention mechanism.
     */
    /** The shared preferences name */
    String SHARED_PREFS = "sdk_prefs";
    /** The preference name for the package name of the application that started the handshake */
    String PREFERENCE_SDK_PCKG_NAME = "WALLET_SDK_NAME";
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
