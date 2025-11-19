require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "FlagshipFeatureFlagsSdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/ds-horizon/flagship-sdks.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"
  s.public_header_files = "ios/FlagshipFeatureFlagsSdk.h"
  s.dependency "React-Core"
  s.dependency 'OpenFeature' , '0.3.0'

  
  s.swift_version = "5.0"
  s.pod_target_xcconfig = {
    'SWIFT_VERSION' => '5.0',
    'DEFINES_MODULE' => 'YES'
  }


  if respond_to?(:install_modules_dependencies, true)
    install_modules_dependencies(s)
  else
    # RN < 0.71: declare the base React dependency yourself.
    s.dependency "React-Core"
  end
end
