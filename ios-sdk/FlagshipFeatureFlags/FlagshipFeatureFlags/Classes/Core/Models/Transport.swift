//
//  Transport.swift
//  Pods
//
//  Created by Atharva Kothawade on 22/09/25.
//

public protocol Transport {    
    func fetchConfig(type: String?) async throws -> Any
}
