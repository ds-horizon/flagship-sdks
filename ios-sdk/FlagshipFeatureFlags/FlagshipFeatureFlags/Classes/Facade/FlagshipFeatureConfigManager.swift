//
//  FlagshipFeatureConfigManager.swift
//  Pods
//
//  Created by Atharva Kothawade on 22/09/25.
//

import Foundation
import CoreData

// MARK: - Configuration Manager (Static Class)

public final class FlagshipFeatureConfigManager {
    
    public static let shared = FlagshipFeatureConfigManager()
    
    private var _config: FlagshipFeatureConfig?
    
    private init() {
        // Private initializer for singleton
    }
        
    public func setConfig(_ config: FlagshipFeatureConfig) {
        self._config = config
    }
    
    public var config: FlagshipFeatureConfig? {
        return _config
    }
    
    public var isConfigured: Bool {
        return _config != nil
    }
    
    public func clearConfig() {
        _config = nil
    }
        
}

public struct FlagshipFeatureConfig {
    public let baseURL: String
    public let refreshInterval: TimeInterval
    public let tenantId: String
    
    public init(baseURL: String, refreshInterval: TimeInterval = 30, tenantId: String) {
        self.baseURL = baseURL
        self.refreshInterval = refreshInterval
        self.tenantId = tenantId
    }
}
