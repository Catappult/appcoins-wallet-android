package com.appcoins.advertising;

/**
* The AppCoinsAdvertising provides an interface with the advertising service from the AppCoins
* Wallet.
*
* The calls to this interface with receive a responde with a response code with the following meaning:
* RESULT_OK = 0 - success
* RESULT_SERVICE_UNAVAILABLE = 1 - The network connection is down
* RESULT_CAMPAIGN_UNAVAILABLE = 2 - The campaign is not available
*/
interface AppCoinsAdvertising {


  /**
  * Provides the campaign ID
  * Given the calling package and the currently selected wallet address, this method return a bundle
  * with the campaign ID available for thar package name if available for that wallet address
  * @return Bundle containing the following key-value pairs
  *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, appropriate response codes
  *                         on failures.
  *         "CAMPAIGN_ID" with a String containing the campaign id
  */
  Bundle getAvailableCampaign();

}