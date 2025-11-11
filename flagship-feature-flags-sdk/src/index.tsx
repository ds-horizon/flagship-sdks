import FlagshipFeatureFlagsSdk from './NativeFlagshipFeatureFlagsSdk';

// Legacy method
export function multiply(a: number, b: number): number {
  return FlagshipFeatureFlagsSdk.multiply(a, b);
}

// Feature flag methods
export async function getBooleanValue(key: string, defaultValue: boolean): Promise<boolean> {
  return await FlagshipFeatureFlagsSdk.getBooleanValue(key, defaultValue);
}

export async function getStringValue(key: string, defaultValue: string): Promise<string> {
  return await FlagshipFeatureFlagsSdk.getStringValue(key, defaultValue);
}

export async function getIntegerValue(key: string, defaultValue: number): Promise<number> {
  return await FlagshipFeatureFlagsSdk.getIntegerValue(key, defaultValue);
}

export async function getDoubleValue(key: string, defaultValue: number): Promise<number> {
  return await FlagshipFeatureFlagsSdk.getDoubleValue(key, defaultValue);
}

export async function getObjectValue(key: string, defaultValue: Object): Promise<Object> {
  return await FlagshipFeatureFlagsSdk.getObjectValue(key, defaultValue);
}
