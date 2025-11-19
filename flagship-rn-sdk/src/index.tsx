import FlagshipRnSdk from './NativeFlagshipRnSdk';
import type { SetContextConfig } from './types';

export function multiply(a: number, b: number): number {
  return FlagshipRnSdk.multiply(a, b);
}

export async function initialize(config: Object): Promise<boolean> {
  return await FlagshipRnSdk.initialize(config);
}

export function setContext(context: SetContextConfig): boolean {
  return FlagshipRnSdk.setContext(context);
}

export function getBooleanValue(key: string, defaultValue: boolean): boolean {
  return FlagshipRnSdk.getBooleanValue(key, defaultValue);
}

export function getStringValue(key: string, defaultValue: string): string {
  return FlagshipRnSdk.getStringValue(key, defaultValue);
}

export function getIntegerValue(key: string, defaultValue: number): number {
  return FlagshipRnSdk.getIntegerValue(key, defaultValue);
}

export function getDoubleValue(key: string, defaultValue: number): number {
  return FlagshipRnSdk.getDoubleValue(key, defaultValue);
}

export function getObjectValue(key: string, defaultValue: Object): Object {
  return FlagshipRnSdk.getObjectValue(key, defaultValue);
}
