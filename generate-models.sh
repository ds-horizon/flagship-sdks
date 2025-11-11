#!/bin/bash

# Script to generate feature flag models for both iOS and Android
# Run from the root directory where feature-flags.schema.json is located

echo "🚀 Generating feature flag models for iOS and Android..."

# Generate iOS Swift models
echo "📱 Generating iOS Swift models..."
quicktype -s schema "feature-flags.schema.json" \
  -l swift \
  --out "ios-sdk/FlagshipFeatureFlags/Core/Models/FeatureFlagModels.swift" \
  --top-level FeatureFlagConfiguration \
  --access-level public \
  --just-types

if [ $? -eq 0 ]; then
    echo "✅ iOS models generated successfully"
else
    echo "❌ Failed to generate iOS models"
    exit 1
fi

# Generate Android Kotlin models
echo "🤖 Generating Android Kotlin models..."
npx -y quicktype@latest -s schema -l kotlin --framework just-types \
  --package com.flagship.android_sdk.core.models "feature-flags.schema.json" \
  > android-sdk/FlagshipSdk/src/main/java/com/flagship/sdk/core/models/FeatureFlagModels.kt
  

if [ $? -eq 0 ]; then
    echo "✅ Android models generated successfully"
else
    echo "❌ Failed to generate Android models"
    exit 1
fi

echo "🎉 All models generated successfully!"
echo ""
echo "Generated files:"
echo "📱 iOS: ios-sdk/FlagshipFeatureFlags/Core/Models/FeatureFlagModels.swift"
echo "🤖 Android: android-sdk/FlagshipSdk/src/main/core/models/FeatureFlagModels.kt"
