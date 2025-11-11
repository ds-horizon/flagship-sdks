#!/bin/bash

# Helper script to sync local tags with remote repository
# Usage: ./scripts/sync-tags.sh [--dry-run]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

DRY_RUN=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        *)
            echo -e "${RED}Error: Unknown argument '$1'${NC}"
            echo -e "${YELLOW}Usage: ./scripts/sync-tags.sh [--dry-run]${NC}"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}🔄 Syncing local tags with remote repository...${NC}"
echo ""

# Fetch latest tags from remote
echo -e "${BLUE}Fetching tags from remote...${NC}"
git fetch --tags --prune

# Get local and remote tags
LOCAL_TAGS=$(git tag | sort)
REMOTE_TAGS=$(git ls-remote --tags origin | grep -v '\^{}' | sed 's/.*refs\/tags\///' | sort)

# Find tags that exist locally but not on remote (to delete)
TAGS_TO_DELETE=$(comm -23 <(echo "$LOCAL_TAGS") <(echo "$REMOTE_TAGS"))

# Find tags that exist on remote but not locally (to fetch)
TAGS_TO_FETCH=$(comm -13 <(echo "$LOCAL_TAGS") <(echo "$REMOTE_TAGS"))

if [ -n "$TAGS_TO_DELETE" ]; then
    echo -e "${YELLOW}Tags to delete locally (don't exist on remote):${NC}"
    echo "$TAGS_TO_DELETE" | sed 's/^/  - /'
    echo ""
    
    if [ "$DRY_RUN" = false ]; then
        read -p "Delete these local tags? (y/N): " -n 1 -r
        echo
        
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "$TAGS_TO_DELETE" | while read -r tag; do
                if [ -n "$tag" ]; then
                    echo -e "${BLUE}Deleting local tag: $tag${NC}"
                    git tag -d "$tag"
                fi
            done
            echo -e "${GREEN}✅ Local tags cleaned up${NC}"
        else
            echo -e "${YELLOW}Skipped deleting local tags${NC}"
        fi
    else
        echo -e "${YELLOW}[DRY RUN] Would delete these local tags${NC}"
    fi
else
    echo -e "${GREEN}✅ No local tags to delete${NC}"
fi

if [ -n "$TAGS_TO_FETCH" ]; then
    echo ""
    echo -e "${BLUE}New tags available on remote:${NC}"
    echo "$TAGS_TO_FETCH" | sed 's/^/  + /'
    echo -e "${GREEN}✅ These are already fetched${NC}"
else
    echo -e "${GREEN}✅ No new remote tags to fetch${NC}"
fi

echo ""
echo -e "${BLUE}Current tags after sync:${NC}"
git tag --sort=-version:refname | head -10 | sed 's/^/  /' || echo "  No tags found"

echo ""
echo -e "${GREEN}✅ Tag sync complete${NC}"
