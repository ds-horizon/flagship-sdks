# Podspec Configuration Guide

## Problem
When using `pod lib lint` with an xcframework in your podspec, you may encounter linker errors like:
```
Undefined symbols for architecture arm64:
  "method descriptor for OpenFeature.FeatureProvider.initialize..."
ld: symbol(s) not found for architecture arm64
```

This happens because xcframeworks with external dependencies (like OpenFeature) don't include the dependency symbols statically linked, causing CocoaPods linting to fail.

## Solution

The podspec has been configured with a flexible approach that allows you to switch between development and distribution modes:

### Development Mode (Default - for linting)
```ruby
use_xcframework = false
```
- Uses source files (`s.source_files`)
- Works perfectly with `pod lib lint`
- Allows development and testing

### Distribution Mode (for releases)
```ruby
use_xcframework = true
```
- Uses pre-built xcframework (`s.ios.vendored_frameworks`)
- For distribution and production releases
- Requires the xcframework to be built and available

## Usage

### For Linting and Development
1. Ensure `use_xcframework = false` in the podspec
2. Run: `pod lib lint FlagshipFeatureFlags.podspec --allow-warnings`

### For Distribution
1. Set `use_xcframework = true` in the podspec
2. Ensure your xcframework is built and available at the specified path
3. Update the source URL to point to your distribution repository

## Why This Works

- **Source files mode**: CocoaPods can compile the source files along with their dependencies (OpenFeature), resolving all symbols at build time
- **XCFramework mode**: The pre-built framework has undefined symbols that need to be resolved by the consuming app, which works in real usage but fails during linting

## Alternative Solutions

If you need to use xcframeworks for linting, you would need to:
1. Build a fat framework that includes OpenFeature statically linked
2. Use a different linting approach that doesn't require linking
3. Create a separate podspec for linting vs distribution

The current solution is the most practical approach for most use cases.
