//
//  TimestampUtility.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 19/11/25.
//  Copyright © 2025 CocoaPods. All rights reserved.
//

import Quick
import Nimble
@testable import FlagshipFeatureFlags

class TimestampUtilitySpec: QuickSpec {
    override class func spec() {
        describe("TimestampUtility") {
            
            beforeEach {
                // Clear timestamp before each test to ensure clean state
                TimestampUtility.clearTimestamp()
            }
            
            afterEach {
                // Clean up after each test
                TimestampUtility.clearTimestamp()
            }
            
            context("when getting stored timestamp") {
                
                it("should return nil when no timestamp is stored") {
                    TimestampUtility.clearTimestamp()
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(beNil())
                }
                
                it("should return stored timestamp correctly") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(testTimestamp))
                }
                
                it("should return nil after clearing timestamp") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    TimestampUtility.clearTimestamp()
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(beNil())
                }
                
                it("should handle zero timestamp") {
                    let testTimestamp: Int64 = 0
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(0))
                }
                
                it("should handle negative timestamp") {
                    let testTimestamp: Int64 = -1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(-1234567890))
                }
                
                it("should handle very large timestamp values") {
                    let testTimestamp: Int64 = Int64.max
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(Int64.max))
                }
                
                it("should handle very small timestamp values") {
                    let testTimestamp: Int64 = Int64.min
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(Int64.min))
                }
                
                it("should handle current timestamp") {
                    let currentTimestamp = Int64(Date().timeIntervalSince1970)
                    TimestampUtility.storeTimestamp(currentTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(currentTimestamp))
                }
                
                it("should persist timestamp across multiple retrievals") {
                    let testTimestamp: Int64 = 9876543210
                    TimestampUtility.storeTimestamp(testTimestamp)
                    
                    let result1 = TimestampUtility.getStoredTimestamp()
                    let result2 = TimestampUtility.getStoredTimestamp()
                    let result3 = TimestampUtility.getStoredTimestamp()
                    
                    expect(result1).to(equal(testTimestamp))
                    expect(result2).to(equal(testTimestamp))
                    expect(result3).to(equal(testTimestamp))
                    expect(result1).to(equal(result2))
                    expect(result2).to(equal(result3))
                }
            }
            
            context("when storing timestamp") {
                
                it("should store timestamp correctly") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let retrieved = TimestampUtility.getStoredTimestamp()
                    expect(retrieved).to(equal(testTimestamp))
                }
                
                it("should overwrite previous timestamp") {
                    let firstTimestamp: Int64 = 1111111111
                    let secondTimestamp: Int64 = 2222222222
                    
                    TimestampUtility.storeTimestamp(firstTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(firstTimestamp))
                    
                    TimestampUtility.storeTimestamp(secondTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(secondTimestamp))
                    expect(TimestampUtility.getStoredTimestamp()).toNot(equal(firstTimestamp))
                }
                
                it("should handle storing same timestamp multiple times") {
                    let testTimestamp: Int64 = 1234567890
                    
                    TimestampUtility.storeTimestamp(testTimestamp)
                    TimestampUtility.storeTimestamp(testTimestamp)
                    TimestampUtility.storeTimestamp(testTimestamp)
                    
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(testTimestamp))
                }
                
                it("should handle storing zero timestamp") {
                    let testTimestamp: Int64 = 0
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(0))
                }
                
                it("should handle storing negative timestamp") {
                    let testTimestamp: Int64 = -1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(-1234567890))
                }
                
                it("should handle storing maximum Int64 value") {
                    let testTimestamp: Int64 = Int64.max
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(Int64.max))
                }
                
                it("should handle storing minimum Int64 value") {
                    let testTimestamp: Int64 = Int64.min
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(Int64.min))
                }
                
                it("should handle storing different timestamp values in sequence") {
                    let timestamps: [Int64] = [100, 200, 300, 400, 500]
                    
                    for timestamp in timestamps {
                        TimestampUtility.storeTimestamp(timestamp)
                        expect(TimestampUtility.getStoredTimestamp()).to(equal(timestamp))
                    }
                }
                
                it("should persist timestamp after storing") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    
                    // Simulate app restart by checking persistence
                    let result = TimestampUtility.getStoredTimestamp()
                    expect(result).to(equal(testTimestamp))
                }
            }
            
            context("when checking if timestamp has changed") {
                
                it("should return true when no timestamp is stored") {
                    TimestampUtility.clearTimestamp()
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: 1234567890)
                    expect(result).to(beTrue())
                }
                
                it("should return false when timestamps are the same") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp)
                    expect(result).to(beFalse())
                }
                
                it("should return true when timestamps are different") {
                    let storedTimestamp: Int64 = 1111111111
                    let newTimestamp: Int64 = 2222222222
                    
                    TimestampUtility.storeTimestamp(storedTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should return true when new timestamp is greater") {
                    let storedTimestamp: Int64 = 1000
                    let newTimestamp: Int64 = 2000
                    
                    TimestampUtility.storeTimestamp(storedTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should return true when new timestamp is smaller") {
                    let storedTimestamp: Int64 = 2000
                    let newTimestamp: Int64 = 1000
                    
                    TimestampUtility.storeTimestamp(storedTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should return false when comparing zero to zero") {
                    TimestampUtility.storeTimestamp(0)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: 0)
                    expect(result).to(beFalse())
                }
                
                it("should return true when comparing zero to non-zero") {
                    TimestampUtility.storeTimestamp(0)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: 1234567890)
                    expect(result).to(beTrue())
                }
                
                it("should return true when comparing non-zero to zero") {
                    TimestampUtility.storeTimestamp(1234567890)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: 0)
                    expect(result).to(beTrue())
                }
                
                it("should handle negative timestamps correctly") {
                    let storedTimestamp: Int64 = -1000
                    let newTimestamp: Int64 = -2000
                    
                    TimestampUtility.storeTimestamp(storedTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should return false when negative timestamps are the same") {
                    let testTimestamp: Int64 = -1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp)
                    expect(result).to(beFalse())
                }
                
                it("should handle maximum Int64 value comparison") {
                    TimestampUtility.storeTimestamp(Int64.max)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: Int64.max)
                    expect(result).to(beFalse())
                }
                
                it("should return true when comparing max to different value") {
                    TimestampUtility.storeTimestamp(Int64.max)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: Int64.max - 1)
                    expect(result).to(beTrue())
                }
                
                it("should handle minimum Int64 value comparison") {
                    TimestampUtility.storeTimestamp(Int64.min)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: Int64.min)
                    expect(result).to(beFalse())
                }
                
                it("should return true when comparing min to different value") {
                    TimestampUtility.storeTimestamp(Int64.min)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: Int64.min + 1)
                    expect(result).to(beTrue())
                }
                
                it("should handle very small differences") {
                    let storedTimestamp: Int64 = 1234567890
                    let newTimestamp: Int64 = 1234567891
                    
                    TimestampUtility.storeTimestamp(storedTimestamp)
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: newTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should handle current timestamp comparisons") {
                    let currentTimestamp = Int64(Date().timeIntervalSince1970)
                    TimestampUtility.storeTimestamp(currentTimestamp)
                    
                    // Same timestamp should not have changed
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: currentTimestamp)).to(beFalse())
                    
                    // Different timestamp should have changed
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: currentTimestamp + 1)).to(beTrue())
                }
            }
            
            context("when clearing timestamp") {
                
                it("should clear stored timestamp") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).toNot(beNil())
                    
                    TimestampUtility.clearTimestamp()
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                }
                
                it("should allow storing after clearing") {
                    let firstTimestamp: Int64 = 1111111111
                    let secondTimestamp: Int64 = 2222222222
                    
                    TimestampUtility.storeTimestamp(firstTimestamp)
                    TimestampUtility.clearTimestamp()
                    TimestampUtility.storeTimestamp(secondTimestamp)
                    
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(secondTimestamp))
                }
                
                it("should handle clearing when no timestamp exists") {
                    TimestampUtility.clearTimestamp()
                    TimestampUtility.clearTimestamp() // Clear again
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                }
                
                it("should clear timestamp and allow hasTimestampChanged to return true") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    TimestampUtility.clearTimestamp()
                    
                    let result = TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp)
                    expect(result).to(beTrue())
                }
                
                it("should handle multiple clear operations") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    
                    TimestampUtility.clearTimestamp()
                    TimestampUtility.clearTimestamp()
                    TimestampUtility.clearTimestamp()
                    
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                }
            }
            
            context("when testing integration between methods") {
                
                it("should work correctly with store, get, and clear sequence") {
                    let testTimestamp: Int64 = 1234567890
                    
                    // Store
                    TimestampUtility.storeTimestamp(testTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(testTimestamp))
                    
                    // Check change
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp)).to(beFalse())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp + 1)).to(beTrue())
                    
                    // Clear
                    TimestampUtility.clearTimestamp()
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: testTimestamp)).to(beTrue())
                }
                
                it("should handle store, check change, store again sequence") {
                    let firstTimestamp: Int64 = 1000
                    let secondTimestamp: Int64 = 2000
                    
                    TimestampUtility.storeTimestamp(firstTimestamp)
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: firstTimestamp)).to(beFalse())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: secondTimestamp)).to(beTrue())
                    
                    TimestampUtility.storeTimestamp(secondTimestamp)
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: secondTimestamp)).to(beFalse())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: firstTimestamp)).to(beTrue())
                }
                
                it("should handle complex sequence of operations") {
                    // Initial state
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 1000)).to(beTrue())
                    
                    // Store first timestamp
                    TimestampUtility.storeTimestamp(1000)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(1000))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 1000)).to(beFalse())
                    
                    // Update to second timestamp
                    TimestampUtility.storeTimestamp(2000)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(2000))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 2000)).to(beFalse())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 1000)).to(beTrue())
                    
                    // Clear and verify
                    TimestampUtility.clearTimestamp()
                    expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 3000)).to(beTrue())
                }
            }
            
            context("when testing real-world timestamp scenarios") {
                
                it("should handle Unix epoch timestamp") {
                    let epochTimestamp: Int64 = 0
                    TimestampUtility.storeTimestamp(epochTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(0))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 0)).to(beFalse())
                }
                
                it("should handle typical current timestamp values") {
                    let currentTimestamp = Int64(Date().timeIntervalSince1970)
                    TimestampUtility.storeTimestamp(currentTimestamp)
                    
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(currentTimestamp))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: currentTimestamp)).to(beFalse())
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: currentTimestamp + 1)).to(beTrue())
                }
                
                it("should handle timestamp from year 2000") {
                    let year2000Timestamp: Int64 = 946684800 // Jan 1, 2000
                    TimestampUtility.storeTimestamp(year2000Timestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(year2000Timestamp))
                }
                
                it("should handle timestamp from year 2100") {
                    let year2100Timestamp: Int64 = 4102444800 // Jan 1, 2100
                    TimestampUtility.storeTimestamp(year2100Timestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(year2100Timestamp))
                }
                
                it("should handle millisecond precision timestamps") {
                    let millisecondTimestamp: Int64 = 1234567890123
                    TimestampUtility.storeTimestamp(millisecondTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(millisecondTimestamp))
                }
                
                it("should handle microsecond precision timestamps") {
                    let microsecondTimestamp: Int64 = 1234567890123456
                    TimestampUtility.storeTimestamp(microsecondTimestamp)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(microsecondTimestamp))
                }
            }
            
            context("when testing edge cases and boundary conditions") {
                
                it("should handle rapid successive store operations") {
                    let timestamps: [Int64] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
                    
                    for timestamp in timestamps {
                        TimestampUtility.storeTimestamp(timestamp)
                        expect(TimestampUtility.getStoredTimestamp()).to(equal(timestamp))
                    }
                }
                
                it("should handle alternating store and clear operations") {
                    for i in 1...5 {
                        let timestamp: Int64 = Int64(i * 1000)
                        TimestampUtility.storeTimestamp(timestamp)
                        expect(TimestampUtility.getStoredTimestamp()).to(equal(timestamp))
                        TimestampUtility.clearTimestamp()
                        expect(TimestampUtility.getStoredTimestamp()).to(beNil())
                    }
                }
                
                it("should handle storing after checking change") {
                    let timestamp: Int64 = 1234567890
                    
                    // No timestamp stored initially
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: timestamp)).to(beTrue())
                    
                    // Store timestamp
                    TimestampUtility.storeTimestamp(timestamp)
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: timestamp)).to(beFalse())
                }
                
                it("should handle multiple get operations without side effects") {
                    let testTimestamp: Int64 = 1234567890
                    TimestampUtility.storeTimestamp(testTimestamp)
                    
                    let result1 = TimestampUtility.getStoredTimestamp()
                    let result2 = TimestampUtility.getStoredTimestamp()
                    let result3 = TimestampUtility.getStoredTimestamp()
                    
                    expect(result1).to(equal(testTimestamp))
                    expect(result2).to(equal(testTimestamp))
                    expect(result3).to(equal(testTimestamp))
                }
                
                it("should handle storing zero after non-zero") {
                    TimestampUtility.storeTimestamp(1234567890)
                    TimestampUtility.storeTimestamp(0)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(0))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 0)).to(beFalse())
                }
                
                it("should handle storing non-zero after zero") {
                    TimestampUtility.storeTimestamp(0)
                    TimestampUtility.storeTimestamp(1234567890)
                    expect(TimestampUtility.getStoredTimestamp()).to(equal(1234567890))
                    expect(TimestampUtility.hasTimestampChanged(newTimestamp: 1234567890)).to(beFalse())
                }
            }
        }
    }
}

