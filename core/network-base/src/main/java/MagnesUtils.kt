import android.content.Context
import com.example.network_base.BuildConfig
import lib.android.paypal.com.magnessdk.Environment
import lib.android.paypal.com.magnessdk.MagnesResult
import lib.android.paypal.com.magnessdk.MagnesSDK
import lib.android.paypal.com.magnessdk.MagnesSettings

class MagnesUtils {

  companion object {

    var magnusResult: MagnesResult? = null

    fun start(context: Context) {
      val magnesSettingsBuilder = if (BuildConfig.DEBUG)
        MagnesSettings.Builder(context).setMagnesEnvironment(Environment.SANDBOX)
      else
        MagnesSettings.Builder(context).setMagnesEnvironment(Environment.LIVE)

      MagnesSDK.getInstance().setUp(magnesSettingsBuilder.build())
    }

    fun collectAndSubmit(context: Context): MagnesResult? {
      magnusResult = MagnesSDK.getInstance().collectAndSubmit(context)
      return magnusResult
    }

    fun getMetadataId(): String? {
      return magnusResult?.paypalClientMetaDataId
    }

  }
}
