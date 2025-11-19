import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  initialize(config: Object): Promise<boolean>;
  setContext(context: Object): boolean;
  getBooleanValue(key: string, defaultValue: boolean): boolean;
  getStringValue(key: string, defaultValue: string): string;
  getIntegerValue(key: string, defaultValue: number): number;
  getDoubleValue(key: string, defaultValue: number): number;
  getObjectValue(key: string, defaultValue: Object): Object;
}

export default TurboModuleRegistry.getEnforcing<Spec>('FlagshipRnSdk');
