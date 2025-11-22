public final class HttpTransport: Transport {

    public let baseURL: String
    public let refreshInterval: TimeInterval
    private var pollingManager: PollingManager?

    public init(baseURL: String, refreshInterval: TimeInterval = 30) {
        self.baseURL = baseURL
        self.refreshInterval = refreshInterval
    }

    public func fetchConfig(type: String?) async throws -> Any {
        guard let config = FlagshipFeatureConfigManager.shared.config else {
            throw HttpError.requestFailed("Configuration not set")
        }

        let normalizedBaseURL = baseURL.hasSuffix("/") ? String(baseURL.dropLast()) : baseURL
        let endpointPath = "v1/feature/config"
        let configURL = "\(normalizedBaseURL)/\(endpointPath)"
        
        var requestURL = configURL
        if let configType = type {
            let querySeparator = configURL.contains("?") ? "&" : "?"
            requestURL = "\(configURL)\(querySeparator)type=\(configType)"
        }
        
        let requestHeaders = ["flagship-api-key": config.flagshipApiKey]
        let httpResponse = try await HttpUtility.shared.get(
            url: requestURL,
            headers: requestHeaders,
            maxRetries: 3,
            delay: 0.5
        )

        guard (200..<300).contains(httpResponse.statusCode) else {
            throw HttpError.requestFailed("Status code: \(httpResponse.statusCode)")
        }

        var configData: [String: Any] = [:]
        
        if let responseBody = httpResponse.body, !responseBody.isEmpty {
            let parsedJSON = try JSONSerialization.jsonObject(with: responseBody, options: [])
            if let parsedBody = parsedJSON as? [String: Any] {
                configData = parsedBody
            }
        }
        
        configData["_headers"] = httpResponse.headers
        
        return configData
    }

}
