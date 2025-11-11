//
//  FeatureFlagManager.swift
//  FlagshipFeatureFlagsSdkExample
//
//  Created by Atharva Kothawade on 04/10/25.
//

import Foundation
import FlagshipFeatureFlags
import OpenFeature
import FlagshipFeatureFlagsSdk

class FeatureFlagManager {
    
    static func initialize() {
        let config = FlagshipFeatureConfig(
            baseURL: "http://api-fs-test-flag.d11dev.com/v1/feature/config",
            refreshInterval: 60,
            tenantId: "tenant1"
        )
        
        let provider = FlagshipOpenFeatureProvider(config: config)
        
        OpenFeatureAPI.shared.setProvider(provider: provider)
        
        let context = MutableContext(targetingKey: "3456", structure: MutableStructure(attributes: [
            "user_tier": Value.string("premium"),
            "country": Value.string("US"),
            "user_group": Value.string("beta_testers"),
            "is_logged_in": Value.boolean(true),
            "is_accessibility_user": Value.boolean(true),
            "device": Value.string("mobile"),
            "theme_pref": Value.string("light"),
            "session_count": Value.double(150.0),
            "region": Value.string("US"),
            "userId": Value.integer(3456),
            "app_version":Value.string("2.3.0")
        ]))
        OpenFeatureAPI.shared.setEvaluationContext(evaluationContext: context)
        
        // Get the OpenFeature client and pass it to the RN SDK
        let client: Client = OpenFeatureAPI.shared.getClient()
        FlagshipFeatureClientSetter.setClient(client)
        print("🚀 FeatureFlagManager: SDK initialized successfully")
    }
}
