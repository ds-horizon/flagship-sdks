#import <React/RCTBridgeModule.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import "FlagshipFeatureFlagsSdkSpec.h"
#endif

#ifdef RCT_NEW_ARCH_ENABLED
@interface RCT_EXTERN_MODULE (FlagshipFeatureFlagsSdk,
                              NSObject <NativeFlagshipFeatureFlagsSdkSpec>)
#else
@interface RCT_EXTERN_MODULE (FlagshipFeatureFlagsSdk,
                              NSObject <RCTBridgeModule>)
#endif

+ (BOOL)requiresMainQueueSetup {
  return NO;
}


RCT_EXTERN_METHOD(multiply
                  : (double)a withB
                  : (double)b resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getBooleanValue
                  : (NSString *)key defaultValue
                  : (BOOL)defaultValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getStringValue
                  : (NSString *)key defaultValue
                  : (NSString *)defaultValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getIntegerValue
                  : (NSString *)key defaultValue
                  : (NSNumber *)defaultValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getDoubleValue
                  : (NSString *)key defaultValue
                  : (NSNumber *)defaultValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getObjectValue
                  : (NSString *)key defaultValue
                  : (NSDictionary *)defaultValue resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeFlagshipFeatureFlagsSdkSpecJSI>(
      params);
}
#endif

@end
