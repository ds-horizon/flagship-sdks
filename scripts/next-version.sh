#!/bin/bash

# Helper script to determine the next version for release
# Usage: ./scripts/next-version.sh [android|ios] [patch|minor|major] [--create]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Determine platform, version type, and create flag
PLATFORM=""
VERSION_TYPE=""
CREATE_TAG=false

# Parse arguments
SYNC_TAGS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --create)
            CREATE_TAG=true
            shift
            ;;
        --sync)
            SYNC_TAGS=true
            shift
            ;;
        android|ios)
            PLATFORM=$1
            shift
            ;;
        patch|minor|major)
            VERSION_TYPE=$1
            shift
            ;;
        *)
            echo -e "${RED}Error: Unknown argument '$1'${NC}"
            exit 1
            ;;
    esac
done

if [ -z "$PLATFORM" ]; then
    echo -e "${YELLOW}Usage: ./scripts/next-version.sh [android|ios] [patch|minor|major] [--create] [--sync]${NC}"
    echo -e "${YELLOW}Examples:${NC}"
    echo "  ./scripts/next-version.sh android patch"
    echo "  ./scripts/next-version.sh ios minor"
    echo "  ./scripts/next-version.sh android patch --create"
    echo "  ./scripts/next-version.sh android minor --sync    # Sync tags with remote first"
    echo ""
    echo -e "${BLUE}Recent Android releases:${NC}"
    git tag --sort=-version:refname --list "android-v*" | head -3 || echo "No Android releases"
    echo ""
    echo -e "${BLUE}Recent iOS releases:${NC}"
    git tag --sort=-version:refname --list "ios-v*" | head -3 || echo "No iOS releases"
    echo ""
    echo -e "${YELLOW}💡 If tags seem out of sync, run: ./scripts/sync-tags.sh${NC}"
    exit 0
fi

# Validate platform
if [[ "$PLATFORM" != "android" && "$PLATFORM" != "ios" ]]; then
    echo -e "${RED}Error: Platform must be 'android' or 'ios'${NC}"
    exit 1
fi

# Sync tags with remote if requested
if [ "$SYNC_TAGS" = true ]; then
    echo -e "${BLUE}🔄 Syncing tags with remote...${NC}"
    git fetch --tags --prune
    echo -e "${GREEN}✅ Tags synced${NC}"
    echo ""
fi

# Get current latest version for the platform
CURRENT=$(git tag --sort=-version:refname --list "${PLATFORM}-v*" | head -1 2>/dev/null)
if [ -z "$CURRENT" ]; then
    CURRENT="${PLATFORM}-v0.0.0"
    echo -e "${BLUE}Current ${PLATFORM} version: ${GREEN}None (will start from v0.1.0)${NC}"
    MAJOR=0
    MINOR=0
    PATCH=0
else
    echo -e "${BLUE}Current ${PLATFORM} version: ${GREEN}$CURRENT${NC}"
    # Parse version numbers (remove platform-v prefix)
    VERSION=${CURRENT#${PLATFORM}-v}
    IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
fi

# Calculate next versions
NEXT_PATCH="${PLATFORM}-v$MAJOR.$MINOR.$((PATCH + 1))"
NEXT_MINOR="${PLATFORM}-v$MAJOR.$((MINOR + 1)).0"
NEXT_MAJOR="${PLATFORM}-v$((MAJOR + 1)).0.0"

# Determine the tag to create
TAG_TO_CREATE=""

# If version type provided, show specific version
if [ -n "$VERSION_TYPE" ]; then
    case $VERSION_TYPE in
        patch)
            echo -e "${GREEN}Next patch version: $NEXT_PATCH${NC}"
            TAG_TO_CREATE="$NEXT_PATCH"
            ;;
        minor)
            echo -e "${GREEN}Next minor version: $NEXT_MINOR${NC}"
            TAG_TO_CREATE="$NEXT_MINOR"
            ;;
        major)
            echo -e "${GREEN}Next major version: $NEXT_MAJOR${NC}"
            TAG_TO_CREATE="$NEXT_MAJOR"
            ;;
        *)
            echo -e "${RED}Error: Invalid version type. Use 'patch', 'minor', or 'major'${NC}"
            exit 1
            ;;
    esac
    
    # If not creating tag, just output the version
    if [ "$CREATE_TAG" = false ]; then
        echo "$TAG_TO_CREATE"
    fi
else
    # Show all options for the platform
    echo -e "${YELLOW}Available next ${PLATFORM} versions:${NC}"
    echo -e "  ${GREEN}Patch:${NC}  $NEXT_PATCH  (bug fixes)"
    echo -e "  ${GREEN}Minor:${NC}  $NEXT_MINOR  (new features, backward compatible)"
    echo -e "  ${GREEN}Major:${NC}  $NEXT_MAJOR  (breaking changes)"
    echo ""
    echo -e "${YELLOW}Usage examples:${NC}"
    echo "  ./scripts/next-version.sh $PLATFORM patch"
    echo "  ./scripts/next-version.sh $PLATFORM patch --create"
    echo "  git tag $NEXT_PATCH && git push origin $NEXT_PATCH"
fi

# Show recent releases for the platform
echo ""
echo -e "${BLUE}Recent ${PLATFORM} releases:${NC}"
git tag --sort=-version:refname --list "${PLATFORM}-v*" | head -5 || echo "No previous ${PLATFORM} releases"

# Handle tag creation if --create flag is provided
if [ "$CREATE_TAG" = true ]; then
    if [ -z "$TAG_TO_CREATE" ]; then
        echo -e "${RED}Error: --create flag requires specifying version type (patch|minor|major)${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${YELLOW}=== TAG CREATION ===${NC}"
    echo -e "${BLUE}About to create and push tag: ${GREEN}$TAG_TO_CREATE${NC}"
    echo ""
    
    # Check if tag already exists
    if git rev-parse "$TAG_TO_CREATE" >/dev/null 2>&1; then
        echo -e "${RED}Error: Tag $TAG_TO_CREATE already exists!${NC}"
        exit 1
    fi
    
    # Check if working directory is clean
    if ! git diff-index --quiet HEAD --; then
        echo -e "${YELLOW}Warning: You have uncommitted changes in your working directory.${NC}"
        echo -e "${YELLOW}It's recommended to commit or stash changes before creating a release tag.${NC}"
        echo ""
    fi
    
    # Show current branch
    CURRENT_BRANCH=$(git branch --show-current)
    echo -e "${BLUE}Current branch: ${GREEN}$CURRENT_BRANCH${NC}"
    
    # Show what will happen
    echo -e "${YELLOW}This will:${NC}"
    echo "  1. Create tag: $TAG_TO_CREATE"
    echo "  2. Push tag to origin"
    echo "  3. Trigger GitHub Actions release workflow"
    echo ""
    
    # Confirmation prompt
    read -p "Do you want to proceed? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Creating tag $TAG_TO_CREATE...${NC}"
        if git tag "$TAG_TO_CREATE"; then
            echo -e "${GREEN}✅ Tag created successfully${NC}"
        else
            echo -e "${RED}❌ Failed to create tag${NC}"
            exit 1
        fi
        
        echo -e "${BLUE}Pushing tag to origin...${NC}"
        if git push origin "$TAG_TO_CREATE"; then
            echo -e "${GREEN}✅ Tag pushed successfully${NC}"
        else
            echo -e "${RED}❌ Failed to push tag${NC}"
            echo -e "${YELLOW}💡 You may need to delete the local tag: git tag -d $TAG_TO_CREATE${NC}"
            exit 1
        fi
        
        echo ""
        echo -e "${GREEN}✅ Successfully created and pushed tag: $TAG_TO_CREATE${NC}"
        echo -e "${BLUE}🚀 GitHub Actions workflow should start shortly.${NC}"
        echo -e "${BLUE}📦 Check the progress at: https://github.com/dream11/flagship-sdk/actions${NC}"
        echo ""
        echo -e "${YELLOW}💡 If the release job fails:${NC}"
        echo "   1. Re-run the failed workflow from GitHub Actions"
        echo "   2. Or delete and recreate tag: git tag -d $TAG_TO_CREATE && git push origin :refs/tags/$TAG_TO_CREATE"
        echo "   3. Then run this script again"
    else
        echo -e "${YELLOW}Tag creation cancelled.${NC}"
        exit 0
    fi
fi
