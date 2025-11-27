#import "FlagshipRnSdk.h"
#import <React/RCTBridgeModule.h>
#import "FlagshipRnSdkSpec.h"

@class FlagshipRnSdkImpl;

@interface FlagshipRnSdkImpl : NSObject
+ (instancetype)shared;
- (NSNumber *)multiply:(double)a b:(double)b;
- (void)initialize:(NSDictionary *)config resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
- (NSNumber *)setContext:(NSDictionary *)context;
- (NSNumber *)getBooleanValue:(NSString *)key defaultValue:(BOOL)defaultValue;
- (NSString *)getStringValue:(NSString *)key defaultValue:(NSString *)defaultValue;
- (NSNumber *)getIntegerValue:(NSString *)key defaultValue:(double)defaultValue;
- (NSNumber *)getDoubleValue:(NSString *)key defaultValue:(double)defaultValue;
- (NSDictionary *)getObjectValue:(NSString *)key defaultValue:(NSDictionary *)defaultValue;
@end

@interface FlagshipRnSdk ()
#ifdef __has_include
#if __has_include("FlagshipRnSdkSpec.h")
<NativeFlagshipRnSdkSpec>
#endif
#endif
@end

@implementation FlagshipRnSdk

- (NSNumber *)multiply:(double)a b:(double)b {
  return [[FlagshipRnSdkImpl shared] multiply:a b:b];
}

- (void)initialize:(NSDictionary *)config
           resolve:(RCTPromiseResolveBlock)resolve
            reject:(RCTPromiseRejectBlock)reject {
  [[FlagshipRnSdkImpl shared] initialize:config resolve:resolve reject:reject];
}

- (NSNumber *)setContext:(NSDictionary *)context {
  return [[FlagshipRnSdkImpl shared] setContext:context];
}

- (NSNumber *)getBooleanValue:(NSString *)key defaultValue:(BOOL)defaultValue {
  return [[FlagshipRnSdkImpl shared] getBooleanValue:key defaultValue:defaultValue];
}

- (NSString *)getStringValue:(NSString *)key defaultValue:(NSString *)defaultValue {
  return [[FlagshipRnSdkImpl shared] getStringValue:key defaultValue:defaultValue];
}

- (NSNumber *)getIntegerValue:(NSString *)key defaultValue:(double)defaultValue {
  return [[FlagshipRnSdkImpl shared] getIntegerValue:key defaultValue:defaultValue];
}

- (NSNumber *)getDoubleValue:(NSString *)key defaultValue:(double)defaultValue {
  return [[FlagshipRnSdkImpl shared] getDoubleValue:key defaultValue:defaultValue];
}

- (NSDictionary *)getObjectValue:(NSString *)key defaultValue:(NSDictionary *)defaultValue {
  return [[FlagshipRnSdkImpl shared] getObjectValue:key defaultValue:defaultValue];
}

#if __has_include("FlagshipRnSdkSpec.h")
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeFlagshipRnSdkSpecJSI>(params);
}
#endif

+ (NSString *)moduleName {
  return @"FlagshipRnSdk";
}

@end

