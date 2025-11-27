import Foundation
import React
import UIKit

@objc(NativeNavigation)
class NativeNavigationModule: NSObject {
    
    @objc
    func openNativeFeatureFlagScreen() {
        DispatchQueue.main.async {
            guard let rootViewController = UIApplication.shared.keyWindow?.rootViewController else {
                return
            }
            
            let nativeVC = NativeFeatureFlagViewController()
            nativeVC.modalPresentationStyle = .fullScreen
            rootViewController.present(nativeVC, animated: true)
        }
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
}

