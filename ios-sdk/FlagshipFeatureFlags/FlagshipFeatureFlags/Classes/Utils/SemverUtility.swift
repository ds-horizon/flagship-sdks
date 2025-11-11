//
//  SemverUtility.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation

public final class SemverUtility {
    
    public init() {}
    
    public static func isSemver(_ version: String) -> Bool {
        let semverPattern = #"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"#
        let regex = try? NSRegularExpression(pattern: semverPattern)
        let range = NSRange(location: 0, length: version.utf16.count)
        return regex?.firstMatch(in: version, options: [], range: range) != nil
    }
    
    
    public static func compareSemver(userVersion: String, expectedVersion: String) -> Int {
        let userComponents = parseSemver(userVersion)
        let expectedComponents = parseSemver(expectedVersion)
        
        guard let user = userComponents, let expected = expectedComponents else {
            return userVersion.compare(expectedVersion, options: .numeric).rawValue
        }
        
        if user.major != expected.major {
            return user.major < expected.major ? -1 : 1
        }
        
        if user.minor != expected.minor {
            return user.minor < expected.minor ? -1 : 1
        }
        
        if user.patch != expected.patch {
            return user.patch < expected.patch ? -1 : 1
        }
        
        let userPreRelease = user.preRelease ?? ""
        let expectedPreRelease = expected.preRelease ?? ""
        
        if userPreRelease.isEmpty && expectedPreRelease.isEmpty {
            return 0
        } else if userPreRelease.isEmpty {
            return 1
        } else if expectedPreRelease.isEmpty {
            return -1
        } else {
            return userPreRelease.compare(expectedPreRelease, options: .numeric).rawValue
        }
    }
    
    private static func parseSemver(_ version: String) -> (major: Int, minor: Int, patch: Int, preRelease: String?)? {
        let pattern = #"^(\d+)\.(\d+)\.(\d+)(?:-([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"#
        
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: version, range: NSRange(location: 0, length: version.utf16.count)),
              match.numberOfRanges >= 4 else {
            return nil
        }
        
        let majorRange = match.range(at: 1)
        let minorRange = match.range(at: 2)
        let patchRange = match.range(at: 3)
        let preReleaseRange = match.range(at: 4)
        
        guard let majorString = Range(majorRange, in: version),
              let minorString = Range(minorRange, in: version),
              let patchString = Range(patchRange, in: version),
              let major = Int(String(version[majorString])),
              let minor = Int(String(version[minorString])),
              let patch = Int(String(version[patchString])) else {
            return nil
        }
        
        let preRelease: String?
        if preReleaseRange.location != NSNotFound {
            preRelease = String(version[Range(preReleaseRange, in: version)!])
        } else {
            preRelease = nil
        }
        
        return (major: major, minor: minor, patch: patch, preRelease: preRelease)
    }
    
    public static func compareSemverStrings(
        userString: String,
        expectedString: String,
        comparator: (Int) -> Bool
    ) -> Bool? {
        guard isSemver(userString) && isSemver(expectedString) else {
            return nil
        }
        let comparisonResult = compareSemver(userVersion: userString, expectedVersion: expectedString)
        return comparator(comparisonResult)
    }
}
