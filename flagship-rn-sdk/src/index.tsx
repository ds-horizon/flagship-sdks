import FlagshipRnSdk from './NativeFlagshipRnSdk';
import type { InitializeConfig, SetContextConfig } from './types';

export function multiply(a: number, b: number): number {
  return FlagshipRnSdk.multiply(a, b);
}

export async function initialize(config: InitializeConfig): Promise<boolean> {
  return await FlagshipRnSdk.initialize(config);
}

export async function setContext(context: SetContextConfig): Promise<boolean> {
  return await FlagshipRnSdk.setContext(context);
}

export async function getBooleanValue(key: string, defaultValue: boolean): Promise<boolean> {
  return await FlagshipRnSdk.getBooleanValue(key, defaultValue);
}

export async function getStringValue(key: string, defaultValue: string): Promise<string> {
  return await FlagshipRnSdk.getStringValue(key, defaultValue);
}

export async function getIntegerValue(key: string, defaultValue: number): Promise<number> {
  return await FlagshipRnSdk.getIntegerValue(key, defaultValue);
}

export async function getDoubleValue(key: string, defaultValue: number): Promise<number> {
  return await FlagshipRnSdk.getDoubleValue(key, defaultValue);
}

export async function getObjectValue(key: string, defaultValue: Object): Promise<Object> {
  return await FlagshipRnSdk.getObjectValue(key, defaultValue);
}
