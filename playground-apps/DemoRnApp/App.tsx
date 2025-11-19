import { useEffect, useState } from 'react';
import { Text, View, StyleSheet, TouchableOpacity, Platform } from 'react-native';
import { multiply, initialize, setContext, getBooleanValue, getStringValue, getDoubleValue, getObjectValue } from '@d11/flagship-rn-sdk';

const result = multiply(3, 7);

export default function App() {
  const [isInitialized, setIsInitialized] = useState(false);
  const [initError, setInitError] = useState<string | null>(null);
  const [darkModeEnabled, setDarkModeEnabled] = useState<boolean | null>(null);
  const [stringValue, setStringValue] = useState<string | null>(null);
  const [doubleValue, setDoubleValue] = useState<number | null>(null);
  const [objectValue, setObjectValue] = useState<any | null>(null);
  const [contextVariant, setContextVariant] = useState<'default' | 'alternate'>('default');

  useEffect(() => {
    const baseUrl = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
    initialize({
      baseUrl,
      tenantId: 'tenant1',
      refreshInterval: 60000,
    })
      .then(() => {
        setIsInitialized(true);
        console.log('FlagshipRnSdk initialized successfully');
        
        setContext({
          targetingKey: '3456',
          user_tier: 'premium',
          country: 'US',
          user_group: 'beta_testers',
          is_logged_in: true,
          is_accessibility_user: true,
          device: 'mobile',
          theme_pref: 'light',
          session_count: 150.0,
          region: 'US',
          userId: 3456,
          app_version: '2.3.0',
          profile: {
            age: 31,
            city: 'Mumbai',
            skills: ['Kotlin', 'React Native', 'TypeScript'],
          },
        })
          .then(() => {
            console.log('FlagshipRnSdk context set successfully');
          })
          .catch((error) => {
            console.error('FlagshipRnSdk setContext failed:', error);
          });
      })
      .catch((error) => {
        setInitError(error.message || 'Initialization failed');
        console.error('FlagshipRnSdk initialization failed:', error);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Flagship RN SDK Example</Text>
      <Text style={styles.result}>Multiply Result: {result}</Text>
      <View style={styles.statusContainer}>
        <Text style={styles.statusLabel}>SDK Status:</Text>
        {isInitialized ? (
          <Text style={styles.statusSuccess}>✓ Initialized</Text>
        ) : initError ? (
          <Text style={styles.statusError}>✗ Error: {initError}</Text>
        ) : (
          <Text style={styles.statusPending}>⏳ Initializing...</Text>
        )}
      </View>
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          const newContext = contextVariant === 'default' 
            ? {
                targetingKey: '3456',
                user_tier: 'premium',
                country: 'IN',
                user_group: 'regular_users',
                is_logged_in: false,
                is_accessibility_user: false,
                device: 'tablet',
                theme_pref: 'dark',
                session_count: 5.0,
                region: 'EU',
                userId: 3456,
                app_version: '1.8.0',
                profile: {
                  age: 31,
                  city: 'Mumbai',
                  skills: ['Kotlin', 'React Native', 'TypeScript'],
                },
              }
            : {
                targetingKey: '3456',
                user_tier: 'premium',
                country: 'US',
                user_group: 'beta_testers',
                is_logged_in: true,
                is_accessibility_user: true,
                device: 'mobile',
                theme_pref: 'light',
                session_count: 150.0,
                region: 'US',
                userId: 3456,
                app_version: '2.3.0',
                profile: {
                  age: 31,
                  city: 'Mumbai',
                  skills: ['Kotlin', 'React Native', 'TypeScript'],
                },
              };
          
          setContext(newContext)
            .then(() => {
              setContextVariant(contextVariant === 'default' ? 'alternate' : 'default');
              setDarkModeEnabled(null);
              setStringValue(null);
              setDoubleValue(null);
              setObjectValue(null);
              console.log('FlagshipRnSdk context updated successfully');
            })
            .catch((error) => {
              console.error('FlagshipRnSdk setContext failed:', error);
            });
        }}
        disabled={!isInitialized}
      >
        <Text style={styles.buttonText}>
          Update Context ({contextVariant === 'default' ? 'Switch to Alternate' : 'Switch to Default'})
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          getBooleanValue('dark_mode_toggle', false)
            .then((value) => {
              setDarkModeEnabled(value);
              console.log('FlagshipRnSdk getBooleanValue dark_mode_toggle:', value);
            })
            .catch((error) => {
              console.error('FlagshipRnSdk getBooleanValue failed:', error);
              setDarkModeEnabled(null);
            });
        }}
        disabled={!isInitialized}
      >
        <Text style={styles.buttonText}>Evaluate Dark Mode Toggle</Text>
      </TouchableOpacity>
      {darkModeEnabled !== null && (
        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Dark Mode Toggle:</Text>
          <Text style={darkModeEnabled ? styles.statusSuccess : styles.statusPending}>
            {darkModeEnabled ? '✓ Enabled' : '✗ Disabled'}
          </Text>
        </View>
      )}
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          getStringValue('homepage_layout_test', 'default')
            .then((value) => {
              setStringValue(value);
              console.log('FlagshipRnSdk getStringValue homepage_layout_test:', value);
            })
            .catch((error) => {
              console.error('FlagshipRnSdk getStringValue failed:', error);
              setStringValue(null);
            });
        }}
        disabled={!isInitialized}
      >
        <Text style={styles.buttonText}>Evaluate Homepage Layout</Text>
      </TouchableOpacity>
      {stringValue !== null && (
        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Homepage Layout:</Text>
          <Text style={styles.statusSuccess}>Value: {stringValue}</Text>
        </View>
      )}
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          getDoubleValue('search_result_limit', 10.0)
            .then((value) => {
              setDoubleValue(value);
              console.log('FlagshipRnSdk getDoubleValue search_result_limit:', value);
            })
            .catch((error) => {
              console.error('FlagshipRnSdk getDoubleValue failed:', error);
              setDoubleValue(null);
            });
        }}
        disabled={!isInitialized}
      >
        <Text style={styles.buttonText}>Evaluate Search Result Limit</Text>
      </TouchableOpacity>
      {doubleValue !== null && (
        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Search Result Limit:</Text>
          <Text style={styles.statusSuccess}>Value: {doubleValue}</Text>
        </View>
      )}
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          getObjectValue('recommendations_config', { limit: 10, enabled: false })
            .then((value) => {
              setObjectValue(value);
              console.log('FlagshipRnSdk getObjectValue recommendations_config:', value);
            })
            .catch((error) => {
              console.error('FlagshipRnSdk getObjectValue failed:', error);
              setObjectValue(null);
            });
        }}
        disabled={!isInitialized}
      >
        <Text style={styles.buttonText}>Evaluate Recommendations Config</Text>
      </TouchableOpacity>
      {objectValue !== null && (
        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Recommendations Config:</Text>
          <Text style={styles.statusSuccess}>
            {JSON.stringify(objectValue, null, 2)}
          </Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 30,
  },
  result: {
    fontSize: 18,
    marginBottom: 30,
  },
  statusContainer: {
    marginTop: 20,
    alignItems: 'center',
  },
  statusLabel: {
    fontSize: 16,
    marginBottom: 10,
  },
  statusSuccess: {
    fontSize: 16,
    color: 'green',
    fontWeight: 'bold',
  },
  statusError: {
    fontSize: 16,
    color: 'red',
    fontWeight: 'bold',
  },
  statusPending: {
    fontSize: 16,
    color: 'orange',
    fontWeight: 'bold',
  },
  button: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
    marginTop: 20,
    opacity: 1,
  },
  buttonDisabled: {
    opacity: 0.5,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
