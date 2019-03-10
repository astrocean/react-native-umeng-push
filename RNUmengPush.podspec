require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNUmengPush"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  RNUmengPush
                   DESC
  s.homepage     = "https://github.com/astrocean/react-native-umeng-push"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author       = { "author" => "rftstars@qq.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/astrocean/react-native-umeng-push.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  #s.dependency "others"
end

pod 'UMCCommon'
pod 'UMCPush'
pod 'UMCSecurityPlugins'
