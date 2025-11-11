package com.flagshipfeatureflagssdk

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.HashMap

class FlagshipFeatureFlagsSdkPackage : BaseReactPackage() {
  override fun getModule(
    name: String,
    reactContext: ReactApplicationContext,
  ): NativeModule? =
    if (name == FlagshipFeatureFlagsSdkModuleImpl.NAME) {
      FlagshipFeatureFlagsSdkModule(reactContext)
    } else {
      null
    }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider =
    ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      val isTurboModule: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED

      moduleInfos[FlagshipFeatureFlagsSdkModuleImpl.NAME] =
        ReactModuleInfo(
          FlagshipFeatureFlagsSdkModuleImpl.NAME,
          FlagshipFeatureFlagsSdkModuleImpl.NAME,
          false, // canOverrideExistingModule
          false, // needsEagerInit
          false, // isCxxModule
          isTurboModule, // isTurboModule
        )
      moduleInfos
    }
}
