//
//  Semver.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 19/11/25.
//  Copyright © 2025 CocoaPods. All rights reserved.
//


import Quick
import Nimble
@testable import FlagshipFeatureFlags

class SemverUtilitySpec: QuickSpec {
    override class func spec() {
        describe("SemverUtility") {
            
            context("when validating semver format") {
                it("should accept valid semver versions") {
                    let validVersions = [
                        "1.0.0",
                        "2.1.3",
                        "10.20.30",
                        "0.0.1",
                        "1.0.0-alpha",
                        "1.0.0-alpha.1",
                        "1.0.0-alpha.1.2",
                        "1.0.0-beta",
                        "1.0.0-beta.1",
                        "1.0.0-rc.1",
                        "1.0.0+build.1",
                        "1.0.0-alpha+build.1",
                        "1.0.0-alpha.1+build.1.2"
                    ]
                    
                    for version in validVersions {
                        expect(SemverUtility.isSemver(version)).to(beTrue(), description: "Version '\(version)' should be valid semver")
                    }
                }
                
                it("should reject invalid semver versions") {
                    let invalidVersions = [
                        "",
                        "1.0",
                        "1.0.0.0",
                        "1.0.0.0.0",
                        "v1.0.0",
                        "1.0.0-",
                        "1.0.0-.",
                        "1.0.0-..",
                        "1.0.0-+",
                        "1.0.0+",
                        "1.0.0++",
                        "01.0.0",
                        "1.00.0",
                        "1.0.00",
                        "1.0.0-01",
                        "1.0.0-alpha.01",
                        "1.0.0-alpha..1",
                        "1.0.0-alpha.1.",
                        "1.0.0-",
                        "1.0.0+",
                        "1.0.0-+",
                        "1.0.0-+build",
                        "1.0.0-+build.1",
                        "1.0.0-alpha+",
                        "1.0.0-alpha+build.",
                        "1.0.0-alpha+build..1"
                    ]
                    
                    for version in invalidVersions {
                        expect(SemverUtility.isSemver(version)).to(beFalse(), description: "Version '\(version)' should be invalid semver")
                    }
                }
                
                it("should handle edge cases") {
                    expect(SemverUtility.isSemver("0.0.0")).to(beTrue())
                    expect(SemverUtility.isSemver("999.999.999")).to(beTrue())
                    expect(SemverUtility.isSemver("1.0.0-0")).to(beTrue())
                    expect(SemverUtility.isSemver("1.0.0-0.0")).to(beTrue())
                    expect(SemverUtility.isSemver("1.0.0-0.0.0")).to(beTrue())
                    expect(SemverUtility.isSemver("1.0.0-0.0.0.0")).to(beTrue())
                }
            }
            
            context("when comparing semver versions") {
                it("should compare major versions correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "2.0.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "2.0.0", expectedVersion: "1.0.0")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "1.0.0")).to(equal(0))
                }
                
                it("should compare minor versions correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.1.0", expectedVersion: "1.2.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.2.0", expectedVersion: "1.1.0")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.1.0", expectedVersion: "1.1.0")).to(equal(0))
                }
                
                it("should compare patch versions correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.1", expectedVersion: "1.0.2")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.2", expectedVersion: "1.0.1")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.1", expectedVersion: "1.0.1")).to(equal(0))
                }
                
                it("should handle pre-release versions correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha", expectedVersion: "1.0.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "1.0.0-alpha")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha", expectedVersion: "1.0.0-alpha")).to(equal(0))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha", expectedVersion: "1.0.0-beta")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-beta", expectedVersion: "1.0.0-alpha")).to(equal(1))
                }
                
                it("should handle pre-release with numbers correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.1", expectedVersion: "1.0.0-alpha.2")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.2", expectedVersion: "1.0.0-alpha.1")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.1", expectedVersion: "1.0.0-alpha.1")).to(equal(0))
                }
                
                it("should handle build metadata correctly") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0+build.1", expectedVersion: "1.0.0+build.2")).to(equal(0))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0+build.1", expectedVersion: "1.0.0")).to(equal(0))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "1.0.0+build.1")).to(equal(0))
                }
                
                it("should handle complex pre-release versions") {
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.1", expectedVersion: "1.0.0-alpha.1.1")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.1.1", expectedVersion: "1.0.0-alpha.1")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0-alpha.1.1", expectedVersion: "1.0.0-alpha.1.1")).to(equal(0))
                }
                
                it("should handle invalid versions gracefully") {
                    // When one version is invalid, it falls back to string comparison
                    // "invalid" vs "1.0.0" -> "invalid" < "1.0.0" in string comparison = -1
                    expect(SemverUtility.compareSemver(userVersion: "invalid", expectedVersion: "1.0.0")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "invalid")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "invalid1", expectedVersion: "invalid2")).to(equal(-1))
                }
                
                it("should handle edge cases") {
                    expect(SemverUtility.compareSemver(userVersion: "0.0.0", expectedVersion: "0.0.0")).to(equal(0))
                    expect(SemverUtility.compareSemver(userVersion: "0.0.0", expectedVersion: "0.0.1")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "0.0.1", expectedVersion: "0.0.0")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "0.0.0", expectedVersion: "0.1.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "0.1.0", expectedVersion: "0.0.0")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "0.0.0", expectedVersion: "1.0.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "0.0.0")).to(equal(1))
                }
                
                it("should handle empty strings") {
                    expect(SemverUtility.compareSemver(userVersion: "", expectedVersion: "1.0.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: "")).to(equal(1))
                    expect(SemverUtility.compareSemver(userVersion: "", expectedVersion: "")).to(equal(0))
                }
                
                it("should handle whitespace") {
                    expect(SemverUtility.compareSemver(userVersion: " 1.0.0 ", expectedVersion: "1.0.0")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1.0.0", expectedVersion: " 1.0.0 ")).to(equal(1))
                }
            }
            
            context("when testing real-world scenarios") {
                it("should handle typical version progression") {
                    let versions = ["1.0.0-alpha", "1.0.0-beta", "1.0.0-rc.1", "1.0.0"]
                    for i in 0..<versions.count-1 {
                        expect(SemverUtility.compareSemver(userVersion: versions[i], expectedVersion: versions[i+1])).to(equal(-1))
                    }
                }
                
                it("should handle patch version progression") {
                    let versions = ["1.0.0", "1.0.1", "1.0.2", "1.0.3"]
                    for i in 0..<versions.count-1 {
                        expect(SemverUtility.compareSemver(userVersion: versions[i], expectedVersion: versions[i+1])).to(equal(-1))
                    }
                }
                
                it("should handle minor version progression") {
                    let versions = ["1.0.0", "1.1.0", "1.2.0", "1.3.0"]
                    for i in 0..<versions.count-1 {
                        expect(SemverUtility.compareSemver(userVersion: versions[i], expectedVersion: versions[i+1])).to(equal(-1))
                    }
                }
                
                it("should handle major version progression") {
                    let versions = ["1.0.0", "2.0.0", "3.0.0", "4.0.0"]
                    for i in 0..<versions.count-1 {
                        expect(SemverUtility.compareSemver(userVersion: versions[i], expectedVersion: versions[i+1])).to(equal(-1))
                    }
                }
            }
            
            context("when testing performance") {
                it("should handle large version numbers") {
                    expect(SemverUtility.compareSemver(userVersion: "999.999.999", expectedVersion: "1000.1000.1000")).to(equal(-1))
                    expect(SemverUtility.compareSemver(userVersion: "1000.1000.1000", expectedVersion: "999.999.999")).to(equal(1))
                }
                
                it("should handle long pre-release identifiers") {
                    let longPreRelease = "alpha." + String(repeating: "1.", count: 100) + "1"
                    expect(SemverUtility.isSemver("1.0.0-\(longPreRelease)")).to(beTrue())
                }
                
                it("should handle long build metadata") {
                    let longBuild = "build." + String(repeating: "1.", count: 100) + "1"
                    expect(SemverUtility.isSemver("1.0.0+\(longBuild)")).to(beTrue())
                }
            }
        }
    }
}
