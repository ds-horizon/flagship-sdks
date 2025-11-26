import UIKit
import FlagshipRnSdk

class NativeFeatureFlagViewController: UIViewController {
    
    private let statusLabel = UILabel()
    private let flagValueLabel = UILabel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.alignment = .center
        stackView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stackView)
        
        NSLayoutConstraint.activate([
            stackView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stackView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20)
        ])
        
        let titleLabel = UILabel()
        titleLabel.text = "Native iOS Feature Flag Screen"
        titleLabel.font = .systemFont(ofSize: 24, weight: .bold)
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        stackView.addArrangedSubview(titleLabel)
        
        statusLabel.text = "SDK Initialized: \(FlagshipSdk.shared.isInitialized())"
        statusLabel.font = .systemFont(ofSize: 16)
        statusLabel.textAlignment = .center
        stackView.addArrangedSubview(statusLabel)
        
        flagValueLabel.text = "Flag Value: Not evaluated yet"
        flagValueLabel.font = .systemFont(ofSize: 18, weight: .medium)
        flagValueLabel.textAlignment = .center
        stackView.addArrangedSubview(flagValueLabel)
        
        let evaluateButton = createButton(title: "Evaluate dark_mode_toggle", action: #selector(evaluateFlag))
        stackView.addArrangedSubview(evaluateButton)
        
        let setContextButton = createButton(title: "Set Context from Native", action: #selector(setContextFromNative))
        stackView.addArrangedSubview(setContextButton)
        
        let closeButton = createButton(title: "Close", action: #selector(closeScreen))
        closeButton.backgroundColor = .systemGray
        stackView.addArrangedSubview(closeButton)
    }
    
    private func createButton(title: String, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = .systemBlue
        button.layer.cornerRadius = 8
        button.contentEdgeInsets = UIEdgeInsets(top: 12, left: 20, bottom: 12, right: 20)
        button.addTarget(self, action: action, for: .touchUpInside)
        return button
    }
    
    @objc private func evaluateFlag() {
        let value = FlagshipSdk.shared.getBooleanValue(key: "dark_mode_toggle", defaultValue: false)
        flagValueLabel.text = "dark_mode_toggle: \(value)"
    }
    
    @objc private func setContextFromNative() {
        let success = FlagshipSdk.shared.setContext(
            targetingKey: "native-user-ios-789",
            context: [
                "platform": "ios_native",
                "user_tier": "premium",
                "is_logged_in": true,
                "cohort": ["SPORTANS"]
            ]
        )
        statusLabel.text = "Context Set: \(success) | SDK Initialized: \(FlagshipSdk.shared.isInitialized())"
    }
    
    @objc private func closeScreen() {
        dismiss(animated: true)
    }
}

