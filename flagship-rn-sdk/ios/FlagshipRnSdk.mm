#import <React/RCTBridgeModule.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import "FlagshipRnSdkSpec.h"
#endif

#ifdef RCT_NEW_ARCH_ENABLED
@interface RCT_EXTERN_MODULE (FlagshipRnSdk,
                              NSObject <NativeFlagshipRnSdkSpec>)
#else
@interface RCT_EXTERN_MODULE (FlagshipRnSdk,
                              NSObject <RCTBridgeModule>)
#endif

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

RCT_EXTERN__BLOCKING_SYNCHRONOUS_METHOD(multiply
                                        : (double)a
                                        b: (double)b)

RCT_EXTERN_METHOD(initialize
                  : (NSDictionary *)config resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setContext
                  : (NSDictionary *)context resolver
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
  return std::make_shared<facebook::react::NativeFlagshipRnSdkSpecJSI>(params);
}
#endif

@end
