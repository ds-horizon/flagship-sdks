//
//  Store.swift
//  FlagshipFeatureFlags
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation

public protocol Store {
    func getFeatureFlagRulesConfig(for key: String) -> [String: Any]?
    func setFeatureFlagRulesConfig(_ configuration: [String: Any])
    func clearFeatureFlagRulesConfig()
}
