#!/usr/bin/env bash

set -euo pipefail

# Usage: ./bump-version.sh <sdk-type> <current-version>
# sdk-type: android | rnsdk | ios
# current-version: x.y.z format

SDK_TYPE="${1:-}"
CURRENT_VERSION="${2:-}"

if [[ -z "$SDK_TYPE" || -z "$CURRENT_VERSION" ]]; then
  echo "Usage: $0 <sdk-type> <current-version>"
  echo "  sdk-type: android | rnsdk | ios"
  echo "  current-version: x.y.z format"
  exit 1
fi

# Parse version and bump patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
PATCH=$((PATCH + 1))
NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"

echo "Current version: ${CURRENT_VERSION}"
echo "New version: ${NEW_VERSION}"

# Update version in the appropriate file
case "$SDK_TYPE" in
  android)
    FILE="android-sdk/FlagshipSdk/build.gradle.kts"
    sed -i -E "s/val sdkVersion = \"[^\"]+\"/val sdkVersion = \"${NEW_VERSION}\"/" "$FILE"
    ;;
  rnsdk)
    FILE="flagship-rn-sdk/package.json"
    cd flagship-rn-sdk
    npm version --no-git-tag-version "${NEW_VERSION}"
    cd ..
    ;;
  ios)
    FILE="ios-sdk/FlagshipFeatureFlags/FlagshipFeatureFlags.podspec"
    # macOS sed requires empty string after -i
    if [[ "$OSTYPE" == "darwin"* ]]; then
      sed -i '' -E "s/s\.version *= *'[^']+'/s.version          = '${NEW_VERSION}'/" "$FILE"
    else
      sed -i -E "s/s\.version *= *'[^']+'/s.version          = '${NEW_VERSION}'/" "$FILE"
    fi
    ;;
  *)
    echo "Unknown sdk-type: $SDK_TYPE"
    exit 1
    ;;
esac

# Configure git
git config user.name "github-actions[bot]"
git config user.email "github-actions[bot]@users.noreply.github.com"

# Commit and push
git add "$FILE"
git commit -m "chore: bump ${SDK_TYPE}-sdk version to ${NEW_VERSION} [skip ci]"
git push origin HEAD:main

echo "Successfully bumped version to ${NEW_VERSION}"

