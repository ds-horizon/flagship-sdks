#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to auto-detect available test classes
detect_test_classes() {
    local test_dir="Tests"
    if [ -d "$test_dir" ]; then
        grep -h "^class [A-Za-z0-9]*Spec.*QuickSpec" "$test_dir"/*.swift 2>/dev/null | \
        sed 's/^class \([A-Za-z0-9]*Spec\).*/\1/' | \
        sort -u
    fi
}

# Function to show usage
show_usage() {
    echo -e "${BLUE}🧪 Single Test File Runner${NC}"
    echo "=================================================="
    echo -e "${YELLOW}Usage:${NC}"
    echo "  ./run_single_ios_test.sh <test_class_name>"
    echo ""
    echo -e "${CYAN}Example:${NC}"
    echo "  ./run_single_ios_test.sh SemverUtilitySpec"
    echo ""
    
    # Auto-detect and show available test classes
    local available_classes=$(detect_test_classes)
    if [ -n "$available_classes" ]; then
        echo -e "${GREEN}Available test classes (auto-detected):${NC}"
        echo "$available_classes" | while read -r class; do
            echo "  • $class"
        done
    else
        echo -e "${YELLOW}Note: No test classes detected. You can still run any test class name.${NC}"
    fi
    echo ""
    echo -e "${CYAN}Tip:${NC} You can run any test class name - it doesn't need to be in this list!"
    echo ""
}

# Check if test class name is provided
if [ $# -eq 0 ]; then
    show_usage
    exit 1
fi

TEST_CLASS="$1"

echo -e "${BLUE}🧪 Running Single Test: $TEST_CLASS${NC}"
echo "=================================================="

# Run the specific test
echo -e "${YELLOW}🚀 Running $TEST_CLASS tests...${NC}"
output=$(xcodebuild test \
    -workspace FlagshipFeatureFlags.xcworkspace \
    -scheme FlagshipFeatureFlags-Example \
    -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.3.1' \
    -only-testing:FlagshipFeatureFlags_Tests/$TEST_CLASS \
    2>&1)

echo -e "\n${BLUE}📊 TEST RESULTS FOR $TEST_CLASS${NC}"
echo "=================================================="

# Count passed and failed tests
passed_count=$(echo "$output" | grep "Test Case.*passed" | wc -l)
failed_count=$(echo "$output" | grep "Test Case.*failed" | wc -l)
total_count=$((passed_count + failed_count))

# Display passed tests
if [ $passed_count -gt 0 ]; then
    echo -e "\n${GREEN}✅ PASSED TESTS ($passed_count):${NC}"
    echo "=================================================="
    echo "$output" | grep "Test Case.*passed" | while read -r line; do
        # Extract test name
        test_name=$(echo "$line" | sed 's/.*Test Case.*\[FlagshipFeatureFlags_Tests\.//' | sed 's/\] passed.*//')
        echo -e "  ${GREEN}✅${NC} $test_name"
    done
fi

# Display failed tests
if [ $failed_count -gt 0 ]; then
    echo -e "\n${RED}❌ FAILED TESTS ($failed_count):${NC}"
    echo "=================================================="
    echo "$output" | grep "Test Case.*failed" | while read -r line; do
        # Extract test name
        test_name=$(echo "$line" | sed 's/.*Test Case.*\[FlagshipFeatureFlags_Tests\.//' | sed 's/\] failed.*//')
        echo -e "  ${RED}❌${NC} $test_name"
    done
fi

# Final summary
echo -e "\n${BLUE}📊 FINAL SUMMARY FOR $TEST_CLASS${NC}"
echo "=================================================="
echo -e "Total Tests: ${BLUE}$total_count${NC}"
echo -e "Passed: ${GREEN}$passed_count${NC}"
echo -e "Failed: ${RED}$failed_count${NC}"

# Calculate success rate
if [ $total_count -gt 0 ]; then
    success_rate=$(( (passed_count * 100) / total_count ))
    echo -e "Success Rate: ${CYAN}${success_rate}%${NC}"
fi

# Overall result
if [ $total_count -eq 0 ]; then
    echo -e "\n${YELLOW}⚠️  NO TESTS FOUND!${NC}"
    echo -e "${YELLOW}🔍 Possible reasons:${NC}"
    echo -e "  1. Test class name might be incorrect: ${CYAN}$TEST_CLASS${NC}"
    echo -e "  2. Test class might not exist in the test target"
    echo -e "  3. Test class might not be properly configured"
    echo ""
    echo -e "${CYAN}💡 Tip:${NC} Run without arguments to see available test classes:"
    echo -e "  ${BLUE}./run_single_ios_test.sh${NC}"
    exit 1
elif [ $failed_count -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo -e "${GREEN}✨ $TEST_CLASS test execution completed successfully!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ SOME TESTS FAILED!${NC}"
    echo -e "${YELLOW}🔍 Check failed test cases above for details${NC}"
    exit 1
fi