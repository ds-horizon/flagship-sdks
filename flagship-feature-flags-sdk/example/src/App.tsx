import React, { useState } from 'react';
import { Text, View, StyleSheet, Button, Alert } from 'react-native';
import {
  getBooleanValue,
  getStringValue,
  getIntegerValue,
  getDoubleValue,
  getObjectValue,
} from '@d11/flagship-feature-flag';

export default function App() {
  const [result, setResult] = useState('');

  const testBoolean = async () => {
    try {
      const value = await getBooleanValue('dark_mode_toggle', false);
      setResult(`Boolean: ${value}`);
    } catch (error) {
      setResult(`Error: ${error}`);
    }
  };

  const testString = async () => {
    try {
      const value = await getStringValue('min_supported_app_version', 'blue');
      setResult(`String: ${value}`);
    } catch (error) {
      setResult(`Error: ${error}`);
    }
  };

  const testInteger = async () => {
    try {
      const value = await getIntegerValue('search_result_limit', 10);
      setResult(`Integer: ${value}`);
    } catch (error) {
      setResult(`Error: ${error}`);
    }
  };

  const testDouble = async () => {
    try {
      const value = await getDoubleValue('search_result_limit', 99.99);
      setResult(`Double: ${value}`);
    } catch (error) {
      setResult(`Error: ${error}`);
    }
  };

  const testObject = async () => {
    try {
      const value = await getObjectValue('recommendations_config', { theme: 'dark', language: 'en' });
      console.log('Object: ', value);
      setResult(`Object: ${JSON.stringify(value)}`);
    } catch (error) {
      setResult(`Error: ${error}`);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Feature Flags</Text>
      
      <Button title="Test Boolean" onPress={testBoolean} />
      <Button title="Test String" onPress={testString} />
      <Button title="Test Integer" onPress={testInteger} />
      <Button title="Test Double" onPress={testDouble} />
      <Button title="Test Object" onPress={testObject} />
      
      <Text style={styles.result}>{result}</Text>
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
    marginBottom: 40,
  },
  result: {
    fontSize: 18,
    marginTop: 20,
    textAlign: 'center',
  },
});
