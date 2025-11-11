package flagshipfeatureflagssdk.example

import com.flagship.sdk.facade.FlagShipConfig
import com.flagship.sdk.facade.FlagshipProvider
import com.flagshipfeatureflagssdk.FlagShipClientManager
import dev.openfeature.kotlin.sdk.ImmutableContext
import dev.openfeature.kotlin.sdk.OpenFeatureAPI
import dev.openfeature.kotlin.sdk.Value
import java.util.concurrent.TimeUnit

public class FlagShipManager {
  fun initOpenFeature(application: MainApplication) {
    val config =
      FlagShipConfig(
        applicationContext = application,
        baseUrl = "https://api-fs-test-flag.d11dev.com", // Default URL, can be changed
        tenantId = "tenant1", // Default tenant, can be changed
        refreshInterval = TimeUnit.SECONDS.toMillis(10),
      )

    val provider = FlagshipProvider("ABcd", config)

    OpenFeatureAPI.setProvider(provider)

    OpenFeatureAPI.setEvaluationContext(
      ImmutableContext(
        targetingKey = "3456",
        getContext(),
      ),
    )

    // Get the OpenFeature client and pass it to the RN SDK
    FlagShipClientManager.openFeatureClient = OpenFeatureAPI.getClient()
    print("🚀 FeatureFlagManager: SDK initialized successfully")
  }

  private fun getContext(): Map<String, Value> =
    mapOf(
      "user_tier" to Value.String("premium"),
      "country" to Value.String("US"),
      "user_group" to Value.String("beta_testers"),
      "is_logged_in" to Value.Boolean(true),
      "is_accessibility_user" to Value.Boolean(true),
      "device" to Value.String("mobile"),
      "theme_pref" to Value.String("light"),
      "session_count" to Value.Double(150.0),
      "region" to Value.String("US"),
      "userId" to Value.Integer(3456),
      "app_version" to Value.String("2.3.0"),
    )
}
