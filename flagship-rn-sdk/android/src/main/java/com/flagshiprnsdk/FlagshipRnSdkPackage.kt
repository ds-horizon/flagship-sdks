package com.flagshiprnsdk

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.HashMap

class FlagshipRnSdkPackage : BaseReactPackage() {
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == FlagshipRnSdkModuleImpl.NAME) {
      FlagshipRnSdkModule(reactContext)
    } else {
      null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      val isTurboModule: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED

      moduleInfos[FlagshipRnSdkModuleImpl.NAME] = ReactModuleInfo(
        FlagshipRnSdkModuleImpl.NAME,
        FlagshipRnSdkModuleImpl.NAME,
        false,
        false,
        false,
        isTurboModule
      )
      moduleInfos
    }
  }
}
