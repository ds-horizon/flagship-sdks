//
//  ViewController.swift
//  FlagshipFeatureFlags
//
//  Created by 210496608 on 09/21/2025.
//  Copyright (c) 2025 210496608. All rights reserved.
//

import UIKit
import FlagshipFeatureFlags
import OpenFeature
import CoreData

class ViewController: UIViewController {
    
    private let coreDataStore = CoreDataStore()
    private var httpTransport: HttpTransport?
    
    // UI Elements
    private var flagNameTextField: UITextField!
    private var resultLabel: UILabel!
    private var scrollView: UIScrollView!
    private var stackView: UIStackView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupOpenFeature()
        setupUI()
    }
    
    private func setupOpenFeature() {
        // For demo purposes, we'll use a simple in-memory store instead of CoreData
        // This avoids the Core Data setup complexity
        let config = FlagshipFeatureConfig(
            baseURL: "http://localhost:8080",
            refreshInterval: 10,
            tenantId: "tenant1"
        )
        
        // Create our custom provider with config only
        let provider = FlagshipOpenFeatureProvider(config: config)
        
        // Set the provider in OpenFeature
        OpenFeatureAPI.shared.setProvider(provider: provider)

        print("🚀 OpenFeature configured with FlagshipFeatureProvider (tenantId: \(config.tenantId))")
        
        // Set up evaluation context for personalized layout
        let context = MutableContext(targetingKey: "3456", structure: MutableStructure(attributes: [
            "user_tier": Value.string("premium"),
            "country": Value.string("US"),
            "user_group": Value.string("beta_testers"),
            "is_logged_in": Value.boolean(true),
            "is_accessibility_user": Value.boolean(true),
            "device": Value.string("mobile"),
            "theme_pref": Value.string("light"),
            "session_count": Value.integer(150),
            "region": Value.string("US"),
            "userId": Value.integer(3456),
            "app_version": Value.string("2.3.0"),
            "user_tags": Value.list([
                Value.string("early-adopter"),
                Value.string("beta-tester"),
                Value.string("premium")
            ])
        ]))
        OpenFeatureAPI.shared.setEvaluationContext(evaluationContext: context)
        
        print("👤 Evaluation context set with user data")
        
    }
    

    
    private func setupUI() {
        view.backgroundColor = .systemBackground
        
        // Create scroll view
        scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.showsVerticalScrollIndicator = true
        view.addSubview(scrollView)
        
        // Create main stack view
        stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 20
        stackView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.addSubview(stackView)
        
        // Title
        let titleLabel = UILabel()
        titleLabel.text = "Feature Flag Testing"
        titleLabel.font = UIFont.systemFont(ofSize: 28, weight: .bold)
        titleLabel.textAlignment = .center
        titleLabel.textColor = .label
        stackView.addArrangedSubview(titleLabel)
        
        // Flag name input
        let inputLabel = UILabel()
        inputLabel.text = "Enter Flag Name:"
        inputLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        inputLabel.textColor = .label
        stackView.addArrangedSubview(inputLabel)
        
        flagNameTextField = UITextField()
        flagNameTextField.text = "dark_mode_toggle"
        flagNameTextField.placeholder = "e.g., dark_mode_toggle"
        flagNameTextField.borderStyle = .roundedRect
        flagNameTextField.font = UIFont.systemFont(ofSize: 16)
        flagNameTextField.backgroundColor = .systemGray6
        flagNameTextField.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(flagNameTextField)
        
        // Test buttons
        setupTestButtons()
        
        // Result display
        let resultTitleLabel = UILabel()
        resultTitleLabel.text = "Result:"
        resultTitleLabel.font = UIFont.systemFont(ofSize: 18, weight: .semibold)
        resultTitleLabel.textColor = .label
        stackView.addArrangedSubview(resultTitleLabel)
        
        resultLabel = UILabel()
        resultLabel.text = "No flag tested yet"
        resultLabel.font = UIFont.systemFont(ofSize: 16)
        resultLabel.textColor = .systemGray
        resultLabel.numberOfLines = 0
        resultLabel.backgroundColor = .systemGray6
        resultLabel.layer.cornerRadius = 8
        resultLabel.layer.masksToBounds = true
        resultLabel.textAlignment = .center
        resultLabel.translatesAutoresizingMaskIntoConstraints = false
        stackView.addArrangedSubview(resultLabel)
        
        
        // Layout constraints
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            scrollView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            
            stackView.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 20),
            stackView.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -20),
            stackView.widthAnchor.constraint(equalTo: scrollView.widthAnchor),
            
            flagNameTextField.heightAnchor.constraint(equalToConstant: 44),
            resultLabel.heightAnchor.constraint(greaterThanOrEqualToConstant: 60)
        ])
    }
    
    private func setupTestButtons() {
        let testButtonsLabel = UILabel()
        testButtonsLabel.text = "Test Flag Types:"
        testButtonsLabel.font = UIFont.systemFont(ofSize: 18, weight: .semibold)
        testButtonsLabel.textColor = .label
        stackView.addArrangedSubview(testButtonsLabel)
        
        // Boolean test button
        let booleanButton = createTestButton(title: "Test Boolean", color: .systemBlue, action: #selector(testBooleanFlag))
        stackView.addArrangedSubview(booleanButton)
        
        // String test button
        let stringButton = createTestButton(title: "Test String", color: .systemGreen, action: #selector(testStringFlag))
        stackView.addArrangedSubview(stringButton)
        
        // Integer test button
        let integerButton = createTestButton(title: "Test Integer", color: .systemOrange, action: #selector(testIntegerFlag))
        stackView.addArrangedSubview(integerButton)
        
        // Double test button
        let doubleButton = createTestButton(title: "Test Double", color: .systemPurple, action: #selector(testDoubleFlag))
        stackView.addArrangedSubview(doubleButton)
        
        // Object test button
        let objectButton = createTestButton(title: "Test Object", color: .systemTeal, action: #selector(testObjectFlag))
        stackView.addArrangedSubview(objectButton)
    }
    
    
    private func createTestButton(title: String, color: UIColor, action: Selector) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.backgroundColor = color
        button.setTitleColor(.white, for: .normal)
        button.layer.cornerRadius = 8
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        button.addTarget(self, action: action, for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.heightAnchor.constraint(equalToConstant: 50).isActive = true
        return button
    }
    
    @objc private func testBooleanFlag() {
        guard let flagName = flagNameTextField.text, !flagName.isEmpty else {
            updateResult("Please enter a flag name", color: .systemRed)
            return
        }
        
        let client = OpenFeatureAPI.shared.getClient()
        let value = client.getBooleanValue(key: flagName, defaultValue: false)
        updateResult("Boolean: \(value)", color: .systemBlue)
    }
    
    @objc private func testStringFlag() {
        guard let flagName = flagNameTextField.text, !flagName.isEmpty else {
            updateResult("Please enter a flag name", color: .systemRed)
            return
        }
        
        let client = OpenFeatureAPI.shared.getClient()
        let value = client.getStringValue(key: flagName, defaultValue: "default")
        updateResult("String: \(value)", color: .systemGreen)
    }
    
    @objc private func testIntegerFlag() {
        guard let flagName = flagNameTextField.text, !flagName.isEmpty else {
            updateResult("Please enter a flag name", color: .systemRed)
            return
        }
        
        let client = OpenFeatureAPI.shared.getClient()
        let value = client.getIntegerValue(key: flagName, defaultValue: 0)
        updateResult("Integer: \(value)", color: .systemOrange)
    }
    
    @objc private func testDoubleFlag() {
        guard let flagName = flagNameTextField.text, !flagName.isEmpty else {
            updateResult("Please enter a flag name", color: .systemRed)
            return
        }
        
        let client = OpenFeatureAPI.shared.getClient()
        let value = client.getDoubleValue(key: flagName, defaultValue: 0.0)
        updateResult("Double: \(value)", color: .systemPurple)
    }
    
    @objc private func testObjectFlag() {
        guard let flagName = flagNameTextField.text, !flagName.isEmpty else {
            updateResult("Please enter a flag name", color: .systemRed)
            return
        }
        
        let client = OpenFeatureAPI.shared.getClient()
        let defaultObject = Value.structure([
            "layout": Value.string("list"),
            "theme": Value.string("light"),
            "cards": Value.integer(12),
            "enabled": Value.boolean(true),
            "version": Value.string("1.0.0"),
            "features": Value.structure([
                "darkMode": Value.boolean(true),
                "notifications": Value.boolean(true),
                "analytics": Value.boolean(true)
            ])
        ])
        let value = client.getObjectValue(key: flagName, defaultValue: defaultObject)
        updateResult("Object: \(value)", color: .systemTeal)
        let layout = value.asStructure()?["layout"]?.asString() ?? "list"
        print("Layout: \(layout)")
    }
    
    private func updateResult(_ text: String, color: UIColor) {
        resultLabel.text = text
        resultLabel.textColor = color
    }
    
    
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}

