#
# Be sure to run `pod lib lint FlagshipFeatureFlags.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'FlagshipFeatureFlags'
  s.version          = '0.2.92'
  s.summary          = 'FlagshipHorizon OpenFeature adapter for iOS'

  s.description      = <<-DESC
  A minimal OpenFeature provider (adapter) for iOS.
  Use it to register a FeatureProvider with OpenFeature and evaluate flags.
                       DESC

  s.homepage         = 'https://github.com/ds-horizon/flagship-sdks'
  s.license          = { :type => 'MIT', :file => 'ios-sdk/FlagshipFeatureFlags/LICENSE' }
  s.author           = { '210496608' => 'atharva.kothawade@dream11.com' }
  s.source           = { :git => 'https://github.com/ds-horizon/flagship-sdks.git', :tag => s.version.to_s }
  s.source_files = 'ios-sdk/FlagshipFeatureFlags/FlagshipFeatureFlags/Classes/**/*'
  
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


