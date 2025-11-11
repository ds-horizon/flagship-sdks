import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initialize(config: {
    baseUrl: string;
    tenantId: string;
    refreshInterval: number;
  }): Promise<boolean>;
  setContext(context: Object): Promise<boolean>;
  getBooleanValue(key: string, defaultValue: boolean): Promise<boolean>;
  getStringValue(key: string, defaultValue: string): Promise<string>;
  getIntegerValue(key: string, defaultValue: number): Promise<number>;
  getDoubleValue(key: string, defaultValue: number): Promise<number>;
  getObjectValue(key: string, defaultValue: Object): Promise<Object>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('FlagshipRnSdk');
