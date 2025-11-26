import Foundation
import os.log

@objc public final class FlagshipState: NSObject {
    @objc public static let shared = FlagshipState()
    
    private let logger = Logger(subsystem: "com.flagshiprnsdk", category: "FlagshipState")
    private let lock = NSLock()
    private var _isInitialized = false
    
    private override init() {
        super.init()
    }
    
    @objc public var isInitialized: Bool {
        lock.lock()
        defer { lock.unlock() }
        return _isInitialized
    }
    
    @objc @discardableResult
    public func markInitialized() -> Bool {
        lock.lock()
        defer { lock.unlock() }
        
        if _isInitialized {
            logger.warning("SDK already initialized, skipping re-initialization")
            return false
        }
        
        _isInitialized = true
        logger.info("SDK initialized successfully")
        return true
    }
    
    @objc public func reset() {
        lock.lock()
        defer { lock.unlock() }
        _isInitialized = false
        logger.info("SDK state reset")
    }
}

