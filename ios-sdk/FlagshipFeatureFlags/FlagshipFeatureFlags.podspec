#
# Be sure to run `pod lib lint FlagshipFeatureFlags.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'FlagshipFeatureFlags'
  s.version          = '0.2.91'
  s.summary          = 'FlagshipHorizon OpenFeature adapter for iOS'

  s.description      = <<-DESC
  A minimal OpenFeature provider (adapter) for iOS.
  Use it to register a FeatureProvider with OpenFeature and evaluate flags.
                       DESC

  s.homepage         = 'https://github.com/ds-horizon/flagship-sdk-ios-framework'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { '210496608' => 'atharva.kothawade@dream11.com' }
  # DEVELOPMENT CONFIGURATION (source code)
  # s.source           = { :git => 'https://github.com/dream11/flagship-sdk.git', :tag => s.version.to_s }
  # s.source_files = 'FlagshipFeatureFlags/Classes/**/*'
  
  # RELEASE CONFIGURATION (pre-built framework)
  s.source           = { :git => 'https://github.com/ds-horizon/flagship-sdk-ios-framework.git', :tag => s.version.to_s }
  s.ios.vendored_frameworks = 'FlagshipFeatureFlags.xcframework'
  s.ios.deployment_target = '14.0'
  s.swift_version = '5.5'
  s.framework = 'Foundation'


  s.pod_target_xcconfig = {
    'BUILD_LIBRARY_FOR_DISTRIBUTION' => 'YES',
    'SKIP_INSTALL' => 'NO'
  }
  s.dependency 'OpenFeature' , '0.3.0'

  
  # s.resource_bundles = {
  #   'FlagshipFeatureFlags' => ['FlagshipFeatureFlags/Assets/*.png']
  # }

  # s.public_header_files = 'Pod/Classes/**/*.h'
  # s.frameworks = 'UIKit', 'MapKit'
  # s.dependency 'AFNetworking', '~> 2.3'
end


