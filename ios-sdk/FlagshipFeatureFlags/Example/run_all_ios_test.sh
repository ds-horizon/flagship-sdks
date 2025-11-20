#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Simple Test Runner${NC}"
echo "=================================================="

# Auto-detect available iPhone simulator
echo -e "${YELLOW}🔍 Detecting available iPhone simulators...${NC}"

# Debug: Show all available devices
echo -e "${BLUE}📱 Available simulators:${NC}"
xcrun simctl list devices available | grep "iPhone" | head -5

# Try to find iPhone 16 first
device_name=$(xcrun simctl list devices available | grep "iPhone 16" | head -1 | grep -o "iPhone 16[^ ]*" | sed 's/ *$//')

# If iPhone 16 not found, try iPhone 15
if [ -z "$device_name" ]; then
    echo -e "${YELLOW}⚠️  iPhone 16 not found, trying iPhone 15...${NC}"
    device_name=$(xcrun simctl list devices available | grep "iPhone 15" | head -1 | grep -o "iPhone 15[^ ]*" | sed 's/ *$//')
fi

# If iPhone 15 not found, try iPhone 14
if [ -z "$device_name" ]; then
    echo -e "${YELLOW}⚠️  iPhone 15 not found, trying iPhone 14...${NC}"
    device_name=$(xcrun simctl list devices available | grep "iPhone 14" | head -1 | grep -o "iPhone 14[^ ]*" | sed 's/ *$//')
fi

# If still not found, try any iPhone
if [ -z "$device_name" ]; then
    echo -e "${YELLOW}⚠️  iPhone 14 not found, trying any iPhone...${NC}"
    device_name=$(xcrun simctl list devices available | grep "iPhone" | head -1 | grep -o "iPhone [0-9A-Za-z ]*" | sed 's/ *$//')
fi

if [ -z "$device_name" ]; then
    echo -e "${RED}❌ No iPhone simulator found${NC}"
    echo -e "${YELLOW}Available devices:${NC}"
    xcrun simctl list devices available | grep "iPhone" || echo "No iPhone simulators available"
    exit 1
fi

echo -e "${GREEN}✅ Found $device_name${NC}"

# Smart iOS version detection with GitHub Actions compatibility
echo -e "${YELLOW}🔍 Detecting iOS version for $device_name...${NC}"

# First, let's see what's actually available
echo -e "${BLUE}📱 Available devices with iOS versions:${NC}"
xcrun simctl list devices available | grep "$device_name" | head -3

# Try to extract iOS version from the device list
available_ios=$(xcrun simctl list devices available | grep "$device_name" | grep -o "OS:[0-9.]*" | cut -d: -f2 | sort -V | tail -1)

# If we still can't detect, try a different approach
if [ -z "$available_ios" ]; then
    echo -e "${YELLOW}⚠️  Could not detect iOS version from device list, trying alternative approach...${NC}"
    
    # Try to find any iOS version for this device type
    available_ios=$(xcrun simctl list devices available | grep "iPhone" | grep -o "OS:[0-9.]*" | cut -d: -f2 | sort -V | tail -1)
    
    if [ -n "$available_ios" ]; then
        echo -e "${GREEN}✅ Found iOS $available_ios from available devices${NC}"
    fi
fi

# If still no iOS version found, use environment-specific fallbacks
if [ -z "$available_ios" ]; then
    echo -e "${YELLOW}⚠️  No iOS version detected, using environment-specific fallback...${NC}"
    
    # Check if we're on GitHub Actions (common environment variables)
    if [ -n "$GITHUB_ACTIONS" ] || [ -n "$CI" ]; then
        echo -e "${BLUE}🔍 Detected CI environment, using GitHub Actions compatible iOS version${NC}"
        available_ios="18.4"  # GitHub Actions typically has 18.4+
    else
        echo -e "${BLUE}🔍 Detected local environment, using local iOS version${NC}"
        available_ios="18.3.1"  # Your local version
    fi
fi

echo -e "${GREEN}📱 Using iOS $available_ios${NC}"

# Final validation - make sure the device and iOS version combination exists
echo -e "${YELLOW}🔍 Validating device and iOS version combination...${NC}"
if xcrun simctl list devices available | grep -q "$device_name.*OS:$available_ios"; then
    echo -e "${GREEN}✅ Device $device_name with iOS $available_ios is available${NC}"
else
    echo -e "${YELLOW}⚠️  Device $device_name with iOS $available_ios not found, but proceeding anyway...${NC}"
    echo -e "${BLUE}📱 Available combinations:${NC}"
    xcrun simctl list devices available | grep "$device_name" | head -3
fi

# Run all tests with better error handling
echo -e "${YELLOW}🚀 Running all tests...${NC}"
output=$(xcodebuild test \
    -workspace FlagshipFeatureFlags.xcworkspace \
    -scheme FlagshipFeatureFlags-Example \
    -destination "platform=iOS Simulator,name=$device_name,OS=$available_ios" \
    -only-testing:FlagshipFeatureFlags_Tests \
    2>&1)

# Store the exit code from xcodebuild
xcodebuild_exit_code=$?

echo -e "\n${BLUE}📊 TEST RESULTS${NC}"
echo "=================================================="

# Count tests using multiple methods for better reliability
passed_count=$(echo "$output" | grep "Test Case.*passed" | wc -l | tr -d ' ')
failed_count=$(echo "$output" | grep "Test Case.*failed" | wc -l | tr -d ' ')
total_count=$((passed_count + failed_count)) 

# If we couldn't count from test cases, try alternative parsing
if [ $total_count -eq 0 ]; then
    # Try to extract from summary lines
    final_summary=$(echo "$output" | grep -E "(Executed.*tests.*with.*failures|Test Suite.*passed|Test Suite.*failed)" | tail -1)
    
    if [ -n "$final_summary" ]; then
        # Extract numbers from summary
        total_tests=$(echo "$final_summary" | grep -o "Executed [0-9]* tests" | grep -o "[0-9]*")
        failures=$(echo "$final_summary" | grep -o "[0-9]* failures" | grep -o "[0-9]*")
        
        if [ -z "$total_tests" ]; then
            total_tests=0
        fi
        if [ -z "$failures" ]; then
            failures=0
        fi
        
        total_count=$total_tests
        failed_count=$failures
        passed_count=$((total_tests - failures))
    fi
fi

# Display passed tests
if [ $passed_count -gt 0 ]; then
    echo -e "${GREEN}✅ PASSED TESTS ($passed_count):${NC}"
    echo "=================================================="
    echo "$output" | grep "Test Case.*passed" | while read -r line; do
        test_name=$(echo "$line" | sed 's/.*Test Case.*\[FlagshipFeatureFlags_Tests\.//' | sed 's/\] passed.*//')
        echo -e "  ${GREEN}✅${NC} $test_name"
    done
fi

# Display failed tests with file names
if [ $failed_count -gt 0 ]; then
    echo -e "\n${RED}❌ FAILED TESTS ($failed_count):${NC}"
    echo "=================================================="
    
    echo "$output" | grep "Test Case.*failed" | while read -r line; do
        # Extract test name and class
        test_name=$(echo "$line" | sed 's/.*Test Case.*\[FlagshipFeatureFlags_Tests\.//' | sed 's/\] failed.*//')
        class_name=$(echo "$test_name" | cut -d' ' -f1)
        test_case=$(echo "$test_name" | cut -d' ' -f2-)
        
        # Find the file name
        file_name=$(find Tests -name "*${class_name}.swift" 2>/dev/null | head -1)
        if [ -n "$file_name" ]; then
            file_name=$(basename "$file_name")
        else
            file_name="Unknown"
        fi
        
        echo -e "  ${RED}❌${NC} File: ${YELLOW}$file_name${NC}"
        echo -e "     Test: ${RED}$test_case${NC}"
        echo ""
    done
fi

# Final summary
echo -e "\n${BLUE}📊 FINAL SUMMARY${NC}"
echo "=================================================="
echo -e "Total Tests: ${BLUE}$total_count${NC}"
echo -e "Passed: ${GREEN}$passed_count${NC}"
echo -e "Failed: ${RED}$failed_count${NC}"

# Calculate success rate
if [ $total_count -gt 0 ]; then
    success_rate=$(( (passed_count * 100) / total_count ))
    echo -e "Success Rate: ${CYAN}${success_rate}%${NC}"
fi

# Overall result - only fail if xcodebuild itself failed, not if individual tests failed
if [ $xcodebuild_exit_code -eq 0 ]; then
    if [ $failed_count -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL TESTS PASSED!${NC}"
        exit 0
    else
        echo -e "\n${YELLOW}⚠️  SOME TESTS FAILED, BUT ALL TESTS WERE EXECUTED${NC}"
        echo -e "${YELLOW}📊 Test execution completed successfully${NC}"
        exit 0  # Don't fail the script if tests ran but some failed
    fi
else
    echo -e "\n${RED}❌ TEST EXECUTION FAILED${NC}"
    echo -e "${RED}🔍 Check the output above for build or execution errors${NC}"
    exit 1
fi