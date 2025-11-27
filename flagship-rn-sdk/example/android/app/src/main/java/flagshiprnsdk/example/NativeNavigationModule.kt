package flagshiprnsdk.example

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class NativeNavigationModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "NativeNavigation"

    @ReactMethod
    fun openNativeFeatureFlagScreen() {
        val activity = currentActivity ?: return
        val intent = Intent(activity, NativeFeatureFlagActivity::class.java)
        activity.startActivity(intent)
    }
}

