
#!/usr/bin/env bash
set -euo pipefail

# ---- Config ----
WORKSPACE="Example/FlagshipFeatureFlags.xcworkspace"
SCHEME="FlagshipFeatureFlags"

OUT_DIR="FlagshipFeatureFlags/build"
ARC_DIR="$OUT_DIR/archives"
XC_PATH="$OUT_DIR/${SCHEME}.xcframework"
DST_PATH="FlagshipFeatureFlags/${SCHEME}.xcframework"

SIMULATOR_ARM64_ARCHIVE_PATH="${ARC_DIR}/${SCHEME}-Sim-arm64.xcarchive"
IOS_ARCHIVE_PATH="${ARC_DIR}/${SCHEME}-iOS.xcarchive"

# ---- Clean ----
rm -rf "$OUT_DIR" "$DST_PATH"
mkdir -p "$ARC_DIR"

# ---- Common flags (critical ones included) ----
COMMON_FLAGS=(
  -workspace "$WORKSPACE"
  -scheme "$SCHEME"
  -archivePath ${IOS_ARCHIVE_PATH} \
  -configuration Release
  -sdk iphoneos
  MACH_O_TYPE=mh_dylib
  SKIP_INSTALL=NO
  DEBUG_INFORMATION_FORMAT=dwarf-with-dsym
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES
)

COMMON_FLAGS_SIM=(
  -arch arm64 -arch x86_64
  -workspace "$WORKSPACE"
  -scheme "$SCHEME"
  -archivePath ${SIMULATOR_ARM64_ARCHIVE_PATH} \
  -configuration Release
  -sdk iphonesimulator
  MACH_O_TYPE=mh_dylib
  SKIP_INSTALL=NO
  DEBUG_INFORMATION_FORMAT=dwarf-with-dsym
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES
)

echo "📱 Archive (iOS devices)…"
xcodebuild archive "${COMMON_FLAGS[@]}"

echo "🖥️ Archive (iOS Simulator Universal)…"
xcodebuild archive "${COMMON_FLAGS_SIM[@]}"

IOS_FRAMEWORK="${IOS_ARCHIVE_PATH}/Products/Library/Frameworks/${SCHEME}.framework"
SIM_FRAMEWORK="${SIMULATOR_ARM64_ARCHIVE_PATH}/Products/Library/Frameworks/${SCHEME}.framework"
IOS_DSYM="${IOS_ARCHIVE_PATH}/dSYMs/${SCHEME}.framework.dSYM"

echo "🧩 Create XCFramework…"
xcodebuild -create-xcframework \
  -framework "$IOS_FRAMEWORK" \
  -framework "$SIM_FRAMEWORK" \
  -output "$DST_PATH"

# Copy dSYM if available
if [ -d "$IOS_DSYM" ]; then
  echo "📦 Copying debug symbols from: $IOS_DSYM"
  cp -R "$IOS_DSYM" "$DST_PATH/"
  echo "✅ Debug symbols included in XCFramework"
else
  echo "⚠️  Debug symbols not found at: $IOS_DSYM"
  echo "📦 XCFramework created without debug symbols"
fi

echo "✅ Done: $DST_PATH"
