//
//  HttpUtility.swift
//  Pods
//
//  Created by Atharva Kothawade on 23/09/25.
//

import Foundation

public enum HttpError: Error {
    case invalidURL
    case requestFailed(String)
    case noResponse
}

public struct HttpResponse {
    public let statusCode: Int
    public let headers: [AnyHashable: Any]
    public let body: Data?
}

public final class HttpUtility {
    public static let shared = HttpUtility()
    private init() {}

    public func get(
        url: String,
        headers: [String: String] = [:],
        maxRetries: Int = 3,
        delay: TimeInterval = 0.5,
        timeout: TimeInterval = 10
    ) async throws -> HttpResponse {
        guard let url = URL(string: url) else { throw HttpError.invalidURL }

        var lastError: Error?
        for attempt in 1...maxRetries {
            do {
                var request = URLRequest(url: url)
                request.httpMethod = "GET"
                request.timeoutInterval = timeout
                request.cachePolicy = .reloadIgnoringLocalCacheData
                request.setValue("no-cache, no-store, must-revalidate", forHTTPHeaderField: "Cache-Control")
                request.setValue("no-cache", forHTTPHeaderField: "Pragma")
                
                for (key, value) in headers {
                    request.setValue(value, forHTTPHeaderField: key)
                }

                let (data, response) = try await URLSession.shared.data(for: request)
                guard let http = response as? HTTPURLResponse else { throw HttpError.noResponse }

                return HttpResponse(statusCode: http.statusCode, headers: http.allHeaderFields, body: data)
            } catch {
                lastError = error
                if attempt < maxRetries {
                    try? await Task.sleep(nanoseconds: UInt64(delay * 1_000_000_000))
                }
            }
        }
        throw lastError ?? HttpError.requestFailed("Unknown error")
    }
}
