//
//  HashUtility.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 19/11/25.
//  Copyright © 2025 CocoaPods. All rights reserved.
//

import Quick
import Nimble
@testable import FlagshipFeatureFlags

class HashUtilitySpec: QuickSpec {
    override class func spec() {
        describe("HashUtility") {
            
            context("when generating hash for flag") {
                
                it("should be deterministic - same input produces same output") {
                    let testString = "user123:flag456"
                    let firstResult = HashUtility.generateHashForFlag(combinedString: testString)
                    let secondResult = HashUtility.generateHashForFlag(combinedString: testString)
                    let thirdResult = HashUtility.generateHashForFlag(combinedString: testString)
                    
                    expect(firstResult).to(beGreaterThanOrEqualTo(0))
                    expect(firstResult).to(beLessThan(100))
                    expect(secondResult).to(beGreaterThanOrEqualTo(0))
                    expect(secondResult).to(beLessThan(100))
                    expect(thirdResult).to(beGreaterThanOrEqualTo(0))
                    expect(thirdResult).to(beLessThan(100))
                    expect(firstResult).to(equal(secondResult))
                    expect(secondResult).to(equal(thirdResult))
                }
                
                it("should handle empty string") {
                    let result = HashUtility.generateHashForFlag(combinedString: "")
                    expect(result).to(beGreaterThanOrEqualTo(0))
                    expect(result).to(beLessThan(100))
                    
                    // Empty string should be deterministic
                    let result2 = HashUtility.generateHashForFlag(combinedString: "")
                    expect(result).to(equal(result2))
                }
                
                it("should handle single character strings") {
                    let characters = ["a", "A", "0", "1", "!", "@", "#", "$", "%"]
                    
                    for char in characters {
                        let result = HashUtility.generateHashForFlag(combinedString: char)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle special characters") {
                    let specialStrings = [
                        "user@domain.com:flag",
                        "user+test:flag-value",
                        "user_test:flag_value",
                        "user-test:flag-test",
                        "user.test:flag.test",
                        "user/test:flag/test",
                        "user\\test:flag\\test",
                        "user:flag:with:colons",
                        "user;flag;with;semicolons",
                        "user,flag,with,commas"
                    ]
                    
                    for specialString in specialStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: specialString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle unicode characters") {
                    let unicodeStrings = [
                        "用户:标志",
                        "ユーザー:フラグ",
                        "사용자:플래그",
                        "пользователь:флаг",
                        "مستخدم:علم",
                        "user🚀:flag🎯",
                        "café:naïve",
                        "résumé:façade"
                    ]
                    
                    for unicodeString in unicodeStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: unicodeString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle whitespace") {
                    let whitespaceStrings = [
                        " ",
                        "  ",
                        "\t",
                        "\n",
                        "\r",
                        "\r\n",
                        " user : flag ",
                        "user\t:flag",
                        "user\n:flag",
                        "user\r:flag"
                    ]
                    
                    for whitespaceString in whitespaceStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: whitespaceString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle very long strings") {
                    let longStrings = [
                        String(repeating: "a", count: 100),
                        String(repeating: "a", count: 1000),
                        String(repeating: "a", count: 10000),
                        String(repeating: "user:flag", count: 100)
                    ]
                    
                    for longString in longStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: longString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle numeric strings") {
                    let numericStrings = [
                        "123",
                        "1234567890",
                        "0",
                        "000",
                        "999999999999999999"
                    ]
                    
                    for numericString in numericStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: numericString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle case sensitivity") {
                    let caseVariants = [
                        "user:flag",
                        "USER:FLAG",
                        "User:Flag",
                        "uSeR:fLaG"
                    ]
                    
                    var results: [Int] = []
                    for variant in caseVariants {
                        let result = HashUtility.generateHashForFlag(combinedString: variant)
                        results.append(result)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                    
                    // Case should affect the hash
                    // At least some variants should produce different results
                    let uniqueResults = Set(results)
                    expect(uniqueResults.count).to(beGreaterThan(1))
                }
                
                it("should handle typical flag allocation format") {
                    let typicalFormats = [
                        "userId:flagKey",
                        "user-123:feature-flag-1",
                        "visitor-id:my-feature",
                        "anonymous-user:new-feature",
                        "user@example.com:premium-feature"
                    ]
                    
                    for format in typicalFormats {
                        let result = HashUtility.generateHashForFlag(combinedString: format)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle edge case values that might cause overflow") {
                    // Test with strings that might produce large hash values
                    let edgeCaseStrings = [
                        String(repeating: "\u{FF}", count: 100), // Max byte value
                        String(repeating: "\u{00}", count: 100), // Min byte value
                        String(repeating: "A", count: 1000),
                        String(repeating: "Z", count: 1000)
                    ]
                    
                    for edgeCaseString in edgeCaseStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: edgeCaseString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
            }
            
            context("when testing hash distribution") {
                
                it("should produce a reasonable distribution across 0-99 range") {
                    var distribution: [Int: Int] = [:]
                    let testCount = 1000
                    
                    for i in 0..<testCount {
                        let testString = "user\(i):flag\(i)"
                        let result = HashUtility.generateHashForFlag(combinedString: testString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        distribution[result, default: 0] += 1
                    }
                    
                    // Check that we got results across multiple buckets
                    expect(distribution.keys.count).to(beGreaterThan(10))
                    
                    // Check that no single bucket has too many collisions
                    // With 1000 inputs and 100 buckets, expected is 10 per bucket
                    // Allow up to 30 per bucket (3x expected) to account for randomness
                    for (_, count) in distribution {
                        expect(count).to(beLessThan(30))
                    }
                }
                
                it("should handle sequential inputs differently") {
                    var results: [Int] = []
                    for i in 0..<100 {
                        let testString = "user\(i):flag\(i)"
                        let result = HashUtility.generateHashForFlag(combinedString: testString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        results.append(result)
                    }
                    
                    // Most sequential inputs should produce different results
                    let uniqueResults = Set(results)
                    expect(uniqueResults.count).to(beGreaterThan(50))
                }
            }
            
            context("when testing consistency") {
                
                it("should produce consistent results across multiple calls") {
                    let testCases = [
                        "test1",
                        "test2",
                        "user:flag",
                        "",
                        "a",
                        "very long string with many characters that should be handled correctly"
                    ]
                    
                    for testCase in testCases {
                        var results: [Int] = []
                        for _ in 0..<10 {
                            let result = HashUtility.generateHashForFlag(combinedString: testCase)
                            expect(result).to(beGreaterThanOrEqualTo(0))
                            expect(result).to(beLessThan(100))
                            results.append(result)
                        }
                        
                        // All results should be the same
                        let uniqueResults = Set(results)
                        expect(uniqueResults.count).to(equal(1))
                    }
                }
                
                it("should handle similar but different strings differently") {
                    let baseString = "user:flag"
                    var results: Set<Int> = []
                    
                    // Test with slight variations
                    let variations = [
                        baseString,
                        "user:flag ",
                        " user:flag",
                        "user:flag1",
                        "user1:flag",
                        "user:flag2",
                        "user2:flag"
                    ]
                    
                    for variation in variations {
                        let result = HashUtility.generateHashForFlag(combinedString: variation)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        results.insert(result)
                    }
                    
                    // Most variations should produce different results
                    expect(results.count).to(beGreaterThan(3))
                }
            }
            
            context("when testing performance and edge cases") {
                
                it("should handle null bytes and control characters") {
                    let controlCharStrings = [
                        "\0",
                        "\0\0\0",
                        "user\0:flag",
                        "\u{01}\u{02}\u{03}",
                        String(repeating: "\0", count: 100)
                    ]
                    
                    for controlString in controlCharStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: controlString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle strings with only separators") {
                    let separatorStrings = [
                        ":",
                        "::",
                        ":::",
                        ":::" + ":::",
                        "user::flag",
                        ":flag",
                        "user:"
                    ]
                    
                    for separatorString in separatorStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: separatorString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle mixed content types") {
                    let mixedStrings = [
                        "user123:flag-abc",
                        "user-abc:flag123",
                        "user123abc:flag-123-abc",
                        "123abc:abc123",
                        "a1b2c3:d4e5f6"
                    ]
                    
                    for mixedString in mixedStrings {
                        let result = HashUtility.generateHashForFlag(combinedString: mixedString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should handle boundary values in output range") {
                    // Test many inputs to see if we can hit boundary values (0 and 99)
                    var foundZero = false
                    var foundNinetyNine = false
                    
                    for i in 0..<10000 {
                        let testString = "user\(i):flag\(i * 7 + 13)"
                        let result = HashUtility.generateHashForFlag(combinedString: testString)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        
                        if result == 0 {
                            foundZero = true
                        }
                        if result == 99 {
                            foundNinetyNine = true
                        }
                        
                        if foundZero && foundNinetyNine {
                            break
                        }
                    }
                    
                    // With enough samples, we should hit both boundaries
                    // But we don't require it, just check that results are in range
                    expect(foundZero || foundNinetyNine).to(beTrue())
                }
            }
            
            context("when testing real-world scenarios") {
                
                it("should handle typical user ID and flag key combinations") {
                    let scenarios = [
                        ("user-12345", "feature-new-ui"),
                        ("visitor-abc-def-ghi", "flag-experiment-1"),
                        ("anonymous-user", "premium-feature"),
                        ("user@example.com", "beta-feature"),
                        ("123456789", "test-flag")
                    ]
                    
                    for (userId, flagKey) in scenarios {
                        let combined = "\(userId):\(flagKey)"
                        let result = HashUtility.generateHashForFlag(combinedString: combined)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                    }
                }
                
                it("should produce different results for different user-flag pairs") {
                    let userId = "user-123"
                    var flagResults: [String: Int] = [:]
                    
                    for i in 0..<50 {
                        let flagKey = "flag-\(i)"
                        let combined = "\(userId):\(flagKey)"
                        let result = HashUtility.generateHashForFlag(combinedString: combined)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        flagResults[flagKey] = result
                    }
                    
                    // Different flags should produce different hash values (allowing for collisions)
                    let uniqueResults = Set(flagResults.values)
                    expect(uniqueResults.count).to(beGreaterThan(20))
                }
                
                it("should produce different results for different users with same flag") {
                    let flagKey = "feature-flag-1"
                    var userResults: [String: Int] = [:]
                    
                    for i in 0..<50 {
                        let userId = "user-\(i)"
                        let combined = "\(userId):\(flagKey)"
                        let result = HashUtility.generateHashForFlag(combinedString: combined)
                        expect(result).to(beGreaterThanOrEqualTo(0))
                        expect(result).to(beLessThan(100))
                        userResults[userId] = result
                    }
                    
                    // Different users should produce different hash values (allowing for collisions)
                    let uniqueResults = Set(userResults.values)
                    expect(uniqueResults.count).to(beGreaterThan(20))
                }
            }
        }
    }
}

