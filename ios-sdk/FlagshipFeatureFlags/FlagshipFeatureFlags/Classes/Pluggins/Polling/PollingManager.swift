import Foundation

public struct PollingConfig {
    public let interval: TimeInterval
    
    public init(interval: TimeInterval) {
        self.interval = interval
    }
}

public class PollingManager {
    private let config: PollingConfig
    private let pollingBlock: () async -> Void
    private var timer: Timer?
    private var isPolling = false
    
    public init(config: PollingConfig, pollingBlock: @escaping () async -> Void) {
        self.config = config
        self.pollingBlock = pollingBlock
    }
    
    public func start() {
        guard !isPolling else { return }
        
        isPolling = true
        
        // Make first call immediately on background thread
        Task.detached(priority: .background) { [weak self] in
            await self?.pollingBlock()
        }
        
        // Schedule subsequent calls on background thread
        timer = Timer.scheduledTimer(withTimeInterval: config.interval, repeats: true) { [weak self] _ in
            Task.detached(priority: .background) {
                await self?.pollingBlock()
            }
        }
    }
    
    public func stop() {
        isPolling = false
        timer?.invalidate()
        timer = nil
    }
    
    deinit {
        stop()
    }
}
