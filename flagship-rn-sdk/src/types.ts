export interface InitializeConfig {
  baseUrl: string;
  flagshipApiKey: string;
  refreshInterval: number;
}

export type ContextValue = 
  | string 
  | number 
  | boolean 
  | Date 
  | ContextValue[] 
  | { [key: string]: ContextValue };

export interface SetContextConfig {
  targetingKey: string;
  [key: string]: ContextValue;
}

