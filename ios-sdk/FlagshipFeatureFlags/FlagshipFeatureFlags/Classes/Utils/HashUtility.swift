//
//  HashUtility.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import CryptoKit

public final class HashUtility {
    
    public static func generateHashForFlag(combinedString: String) -> Int {
        let digest = Insecure.MD5.hash(data: Data(combinedString.utf8))
        let hashBytes = Array(digest)
        
        var hash: Int64 = 0
        for i in 0..<min(8, hashBytes.count) {
            let byteValue = Int64(hashBytes[i]) & 0xFF
            hash = hash | (byteValue << (i * 8))
        }
        
        let unsignedHash = hash & Int64.max
        let finalValue = Int(unsignedHash % 100)
        return finalValue
    }
}

