const { withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

function withFlagshipRnSdkPodfile(config) {
  return withDangerousMod(config, [
    'ios',
    async (config) => {
      const podfilePath = path.join(config.modRequest.platformProjectRoot, 'Podfile');
      
      if (!fs.existsSync(podfilePath)) {
        return config;
      }
      
      let podfileContent = fs.readFileSync(podfilePath, 'utf8');
      let modified = false;
      
      const hasUseFrameworks = podfileContent.includes('use_frameworks!');
      const hasStaticLinkage = podfileContent.match(/use_frameworks!\s*:linkage\s*=>\s*:static/);
      const hasUnconditionalUseFrameworks = podfileContent.match(/^\s*use_frameworks!\s*$/m);
      
      if (!hasUseFrameworks) {
        const targetMatch = podfileContent.match(/target\s+['"][^'"]+['"]\s+do/);
        if (targetMatch) {
          const insertIndex = targetMatch.index + targetMatch[0].length;
          podfileContent = 
            podfileContent.slice(0, insertIndex) +
            '\n  use_frameworks!\n' +
            podfileContent.slice(insertIndex);
          modified = true;
        }
      } else if (hasStaticLinkage) {
        podfileContent = podfileContent.replace(
          /use_frameworks!\s*:linkage\s*=>\s*:static/,
          'use_frameworks!'
        );
        modified = true;
      } else if (!hasUnconditionalUseFrameworks) {
        const targetMatch = podfileContent.match(/target\s+['"][^'"]+['"]\s+do/);
        if (targetMatch) {
          const insertIndex = targetMatch.index + targetMatch[0].length;
          podfileContent = 
            podfileContent.slice(0, insertIndex) +
            '\n  use_frameworks!\n' +
            podfileContent.slice(insertIndex);
          modified = true;
        }
      }
      
      const postInstallHook = `    installer.pods_project.targets.each do |target|
      if target.name == 'FlagshipFeatureFlags' || target.name == 'OpenFeature'
        target.build_configurations.each do |config|
          config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
        end
      end
    end`;
      
      if (!podfileContent.includes('BUILD_LIBRARY_FOR_DISTRIBUTION')) {
        const reactNativePostInstallPattern = /react_native_post_install\([\s\S]*?\)\s*\n\s*end/m;
        const match = podfileContent.match(reactNativePostInstallPattern);
        
        if (match) {
          const replacement = match[0].replace(/\n\s*end\s*$/, '\n' + postInstallHook + '\n  end');
          podfileContent = podfileContent.replace(reactNativePostInstallPattern, replacement);
          modified = true;
        } else {
          const postInstallPattern = /post_install\s+do\s+\|installer\|[\s\S]*?end/m;
          const postInstallMatch = podfileContent.match(postInstallPattern);
          
          if (postInstallMatch) {
            const replacement = postInstallMatch[0].replace(/\n\s*end\s*$/, '\n' + postInstallHook + '\n  end');
            podfileContent = podfileContent.replace(postInstallPattern, replacement);
            modified = true;
          }
        }
      }
      
      if (modified) {
        fs.writeFileSync(podfilePath, podfileContent);
      }
      
      return config;
    },
  ]);
}

module.exports = withFlagshipRnSdkPodfile;

