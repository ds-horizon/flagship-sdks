# Releasing Flagship SDKs

This document describes how to create releases for both Android and iOS Flagship SDKs in this mono-repo.

## Overview

The release process uses platform-specific Git tags and GitHub Actions for automated publishing:

- **Android**: Published to GitHub Packages Maven repository
- **iOS**: Creates GitHub releases with framework artifacts

Both platforms follow [Semantic Versioning](https://semver.org/) with platform-specific tag prefixes.

## Quick Start

**Complete release workflow example:**

```bash
# 1. Check what versions are available
./scripts/next-version.sh android

# 2. Create and push a new minor release
./scripts/next-version.sh android minor --create

# 3. Monitor the release at:
# https://github.com/dream11/flagship-sdk/actions
```

## Release Process

### 1. Determine Next Version

Use the helper script to see available version options for each platform:

```bash
# See all platforms
./scripts/next-version.sh

# Check Android versions
./scripts/next-version.sh android

# Check iOS versions
./scripts/next-version.sh ios
```

This will show:

- Current version for the platform
- Next patch version (bug fixes)
- Next minor version (new features, backward compatible)
- Next major version (breaking changes)

### 2. Create and Push Release Tag

**Option A: Automated (Recommended)**

```bash
# For Android releases
./scripts/next-version.sh android patch --create   # Bug fixes
./scripts/next-version.sh android minor --create   # New features
./scripts/next-version.sh android major --create   # Breaking changes

# For iOS releases
./scripts/next-version.sh ios patch --create        # Bug fixes
./scripts/next-version.sh ios minor --create        # New features
./scripts/next-version.sh ios major --create        # Breaking changes
```

The `--create` flag will:

- Show you exactly what tag will be created
- Check for uncommitted changes and warn you
- Ask for confirmation before proceeding
- Create and push the tag automatically
- Provide links to monitor the release process

**Option B: Manual**

```bash
# For Android releases
git tag android-v0.1.1 && git push origin android-v0.1.1

# For iOS releases
git tag ios-v0.1.1 && git push origin ios-v0.1.1
```

### 3. Automated Release Process

Once the tag is pushed, GitHub Actions will automatically:

1. **Build**: Compile the Android SDK
2. **Test**: Run all unit tests
3. **Publish**: Upload to GitHub Packages Maven repository
4. **Release**: Create GitHub Release with changelog
5. **Artifacts**: Upload AAR files as release artifacts

### 4. Verify Release

After the workflow completes:

1. Check [GitHub Releases](https://github.com/dream11/flagship-sdk/releases) for the new release
2. Verify the package appears in [GitHub Packages](https://github.com/dream11/flagship-sdk/packages)
3. Test integration in a sample project

## Maintenance Releases (Hotfixes)

For critical fixes to older versions:

```bash
# Create maintenance branch from the tag that needs patching
git checkout v0.1.0
git checkout -b maintenance/0.1.x

# Apply the fix
git cherry-pick <commit-hash>  # or make direct changes
git commit -m "fix: critical security issue"

# Create patch release tag
git tag v0.1.1
git push origin v0.1.1
git push origin maintenance/0.1.x  # keep branch for future patches

# Optionally merge fix forward to main
git checkout main
git cherry-pick <fix-commit-hash>
```

## Using the Released SDK

### For Consumers

**Android Integration:**

Add to your app's `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/dream11/flagship-sdk")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.flagship.sdk:flagship-sdk:0.1.0")
}
```

**iOS Integration:**

Add to your `Podfile`:

```ruby
# If using CocoaPods (when available)
pod 'FlagshipFeatureFlags', '~> 0.1.0'

# Or download framework from GitHub Releases
# https://github.com/dream11/flagship-sdk/releases
```

### Authentication

Users need a GitHub Personal Access Token with `read:packages` permission:

1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate token with `read:packages` scope
3. Add to `gradle.properties` or environment variables:

```properties
# gradle.properties
gpr.user=your-github-username
gpr.key=your-github-token
```

## Version History

All released versions can be viewed:

```bash
# List all versions
git tag --sort=-version:refname

# See what changed between versions
git log v0.1.0..v0.2.0 --oneline

# Check recent releases
./scripts/next-version.sh
```

## Troubleshooting

### Release Failed

If a tag was created but the GitHub Actions workflow failed:

**Quick Recovery:**

```bash
# Use the retry helper script
./scripts/retry-release.sh android-v1.2.3
```

**Manual Options:**

1. **Re-run from GitHub Actions** (Recommended)

   - Go to: https://github.com/dream11/flagship-sdk/actions
   - Find the failed workflow run
   - Click "Re-run jobs" or "Re-run failed jobs"

2. **Delete and recreate tag**

   ```bash
   git tag -d android-v1.2.3
   git push origin :refs/tags/android-v1.2.3
   ./scripts/next-version.sh android patch --create
   ```

3. **Manual release process**
   ```bash
   # For Android
   cd android-sdk
   ./gradlew :FlagshipSdk:publish
   gh release create android-v1.2.3 --title "Android Release 1.2.3" --generate-notes
   ```

**Common failure causes:**

- Tests failing
- Missing GitHub token permissions
- Network issues during publishing
- Invalid workflow configuration

### Package Not Visible

1. Ensure you have access to the repository
2. Check GitHub token has `read:packages` permission
3. Verify the package exists in [GitHub Packages](https://github.com/dream11/flagship-sdk/packages)

### Version Conflicts

**If local and remote tags are out of sync:**

```bash
# Quick sync (recommended)
./scripts/sync-tags.sh

# Or sync during version check
./scripts/next-version.sh android minor --sync
```

**Manual tag cleanup:**

```bash
# Delete local tag
git tag -d android-v1.0.0

# Delete remote tag (be careful!)
git push origin :refs/tags/android-v1.0.0

# Sync all tags
git fetch --tags --prune
```

## Requirements

### Development Environment

- **Java 17** or higher (required by Android Gradle Plugin 8.11.2+)
- **Android SDK** with API level 24+
- **Git** for version control

### Build Tools

- **Gradle 8.13+** (automatically handled by wrapper)
- **Android Gradle Plugin 8.11.2+**
- **Kotlin 2.1.0+**

## Best Practices

1. **Test Before Release**: Always run tests locally before tagging
2. **Meaningful Commits**: Use conventional commits for better changelogs
3. **Version Bumps**: Follow semantic versioning strictly
4. **Documentation**: Update README and docs for new features
5. **Breaking Changes**: Clearly document in release notes
