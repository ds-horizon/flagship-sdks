# @d11/flagship-rn-sdk

A React Native SDK for managing feature flags with support for dynamic configuration, context-based targeting, and real-time flag evaluation.

## Features

- ✅ **Easy Integration**: Simple API for React Native applications
- ✅ **Type-Safe**: Full TypeScript support
- ✅ **Context-Based Targeting**: Evaluate flags based on user context
- ✅ **Multiple Value Types**: Support for boolean, string, integer, double, and object values
- ✅ **Auto-Refresh**: Configurable polling interval for flag updates
- ✅ **Expo Compatible**: Works seamlessly with Expo projects
- ✅ **Cross-Platform**: Works on both iOS and Android

## Installation

```sh
npm install @d11/flagship-rn-sdk
```

or

```sh
yarn add @d11/flagship-rn-sdk
```

## Setup

### React Native (Bare Workflow)

#### iOS

1. Install pods:
```sh
cd ios && pod install && cd ..
```

2. The SDK automatically configures the necessary Podfile settings for static frameworks.

#### Android

No additional setup required for Android.

## Usage

### 1. Initialize the SDK

Initialize the SDK with your configuration before using any flag evaluation methods:

```typescript
import { initialize } from '@d11/flagship-rn-sdk';

await initialize({
  baseUrl: 'https://api.example.com',
  flagshipApiKey: 'your-api-key',
  refreshInterval: 60, // in seconds (default: 30)
});
```

**Configuration Options:**

- `baseUrl` (string, required): Base URL of your feature flag service
- `flagshipApiKey` (string, required): Your Flagship API key
- `refreshInterval` (number, optional): Polling interval in seconds (default: 30)

### 2. Set User Context

Set the user context to enable context-based flag targeting:

```typescript
import { setContext } from '@d11/flagship-rn-sdk';

setContext({
  targetingKey: 'user-123',
  user_tier: 'premium',
  country: 'US',
  is_logged_in: true,
  session_count: 5,
  profile: {
    age: 25,
    city: 'New York',
  },
});
```

**Context Requirements:**

- `targetingKey` (string, required): Unique identifier for the user/context
- Additional fields: Any number of additional context fields (string, number, boolean, Date, array, or object)

### 3. Get Flag Values

Evaluate feature flags with type-safe methods:

#### Boolean Flags

```typescript
import { getBooleanValue } from '@d11/flagship-rn-sdk';

const darkModeEnabled = getBooleanValue('dark_mode', false);
if (darkModeEnabled) {
  // Enable dark mode
}
```

#### String Flags

```typescript
import { getStringValue } from '@d11/flagship-rn-sdk';

const theme = getStringValue('app_theme', 'light');
```

#### Integer Flags

```typescript
import { getIntegerValue } from '@d11/flagship-rn-sdk';

const maxItems = getIntegerValue('max_cart_items', 10);
```

#### Double Flags

```typescript
import { getDoubleValue } from '@d11/flagship-rn-sdk';

const discountRate = getDoubleValue('discount_rate', 0.0);
```

#### Object Flags

```typescript
import { getObjectValue } from '@d11/flagship-rn-sdk';

const config = getObjectValue('app_config', {});
```

### Complete Example

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text } from 'react-native';
import {
  initialize,
  setContext,
  getBooleanValue,
  getStringValue,
} from '@d11/flagship-rn-sdk';

export default function App() {
  const [isReady, setIsReady] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [theme, setTheme] = useState('light');

  useEffect(() => {
    async function setupFlags() {
      // Initialize SDK
      await initialize({
        baseUrl: 'https://api.example.com',
        flagshipApiKey: 'your-api-key',
        refreshInterval: 60,
      });

      // Set user context
      setContext({
        targetingKey: 'user-123',
        user_tier: 'premium',
        country: 'US',
      });

      // Evaluate flags
      const darkModeFlag = getBooleanValue('dark_mode', false);
      const themeFlag = getStringValue('app_theme', 'light');

      setDarkMode(darkModeFlag);
      setTheme(themeFlag);
      setIsReady(true);
    }

    setupFlags();
  }, []);

  if (!isReady) {
    return <Text>Loading...</Text>;
  }

  return (
    <View style={{ backgroundColor: darkMode ? '#000' : '#fff' }}>
      <Text>Theme: {theme}</Text>
    </View>
  );
}
```

## API Reference

### `initialize(config: InitializeConfig): Promise<boolean>`

Initializes the SDK with the provided configuration.

**Parameters:**
- `config.baseUrl` (string): Base URL for the feature flag service
- `config.flagshipApiKey` (string): Flagship API key
- `config.refreshInterval` (number, optional): Polling interval in seconds (default: 30)

**Returns:** `Promise<boolean>` - `true` if initialization succeeds

### `setContext(context: SetContextConfig): boolean`

Sets the user context for flag evaluation.

**Parameters:**
- `context.targetingKey` (string, required): Unique user identifier
- `context[key: string]` (ContextValue): Additional context fields

**Returns:** `boolean` - `true` if context is set successfully

**Context Value Types:**
- `string`
- `number`
- `boolean`
- `Date`
- `ContextValue[]` (array)
- `{ [key: string]: ContextValue }` (object)

### `getBooleanValue(key: string, defaultValue: boolean): boolean`

Gets a boolean flag value.

**Parameters:**
- `key` (string): Flag key
- `defaultValue` (boolean): Default value if flag is not found

**Returns:** `boolean` - Flag value or default

### `getStringValue(key: string, defaultValue: string): string`

Gets a string flag value.

**Parameters:**
- `key` (string): Flag key
- `defaultValue` (string): Default value if flag is not found

**Returns:** `string` - Flag value or default

### `getIntegerValue(key: string, defaultValue: number): number`

Gets an integer flag value.

**Parameters:**
- `key` (string): Flag key
- `defaultValue` (number): Default value if flag is not found

**Returns:** `number` - Flag value or default

### `getDoubleValue(key: string, defaultValue: number): number`

Gets a double/float flag value.

**Parameters:**
- `key` (string): Flag key
- `defaultValue` (number): Default value if flag is not found

**Returns:** `number` - Flag value or default

### `getObjectValue(key: string, defaultValue: Object): Object`

Gets an object flag value.

**Parameters:**
- `key` (string): Flag key
- `defaultValue` (Object): Default value if flag is not found

**Returns:** `Object` - Flag value or default

## TypeScript Support

The SDK is written in TypeScript and includes full type definitions. Import types as needed:

```typescript
import type { InitializeConfig, SetContextConfig, ContextValue } from '@d11/flagship-rn-sdk';
```

## Troubleshooting

### iOS Build Issues

If you encounter build issues on iOS:

1. Clean build folder: `cd ios && xcodebuild clean && cd ..`
2. Reinstall pods: `cd ios && pod deintegrate && pod install && cd ..`
3. Ensure deployment target is iOS 15.1 or higher

### Default Values Always Returned

If flags always return default values:

1. Verify `baseUrl` is correct and accessible
2. Check `flagshipApiKey` matches your configuration
3. Ensure `setContext` is called with valid `targetingKey`
5. Check network connectivity

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
