var fs = require("fs");
var glob = require("glob");
var inquirer = require('inquirer');
var path = require("path");
var plist = require("plist");
var xcode = require("xcode");

var package = require('../../package.json');

module.exports = () => {

    console.log("Running ios postlink script");

    var ignoreNodeModules = { ignore: "node_modules/**" };
    var ignoreNodeModulesAndPods = { ignore: ["node_modules/**", "ios/Pods/**"] };
    var appDelegatePaths = glob.sync("**/AppDelegate.+(mm|m)", ignoreNodeModules);

    // Fix for https://github.com/Microsoft/react-native-code-push/issues/477
    // Typical location of AppDelegate.m for newer RN versions: $PROJECT_ROOT/ios/<project_name>/AppDelegate.m
    // Let's try to find that path by filtering the whole array for any path containing <project_name>
    // If we can't find it there, play dumb and pray it is the first path we find.
    var appDelegatePath = findFileByAppName(appDelegatePaths, package ? package.name : null) || appDelegatePaths[0];

    if (!appDelegatePath) {
        return Promise.reject(`Couldn't find AppDelegate. You might need to update it manually \
    Please refer to plugin configuration section for iOS at \
    https://github.com/microsoft/react-native-code-push#plugin-configuration-ios`);
    }

    var appDelegateContents = fs.readFileSync(appDelegatePath, "utf8");
 
    // 1. Add the header import statement
    var umengPushHeaderImportStatement = `#import <Umeng/RNUMConfigure.h>;\n#import <Umeng/UMPushModule.h>;\n#import <UMPush/UMessage.h>;`;
    if (~appDelegateContents.indexOf(umengPushHeaderImportStatement)) {
        console.log(`"RNUMConfigure.h" header already imported.`);
    } else {
        var appDelegateHeaderImportStatement = `#import "AppDelegate.h"`;
        appDelegateContents = appDelegateContents.replace(appDelegateHeaderImportStatement,
            `${appDelegateHeaderImportStatement}\n${umengPushHeaderImportStatement}`);
    }


    // 2. Modify jsCodeLocation value assignment
    var configUmeng =`
    //UMeng初始化
    //[UMConfigure setLogEnabled:YES];
    [RNUMConfigure init];

    // Push组件基本功能配置
    UMessageRegisterEntity * entity = [[UMessageRegisterEntity alloc] init];
    //type是对推送的几个参数的选择，可以选择一个或者多个。默认是三个全部打开，即：声音，弹窗，角标
    entity.types = UMessageAuthorizationOptionBadge|UMessageAuthorizationOptionSound|UMessageAuthorizationOptionAlert;
    [UNUserNotificationCenter currentNotificationCenter].delegate=self;
    [UMessage registerForRemoteNotificationsWithLaunchOptions:launchOptions Entity:entity completionHandler:^(BOOL granted, NSError * _Nullable error) {
        if (granted) {
        }else{
        }
    }];

    //消息统计
    //iOS10以下使用这两个方法接收通知
    -(void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
    {
        [UMessage setAutoAlert:NO];
        if([[[UIDevice currentDevice] systemVersion]intValue] < 10){
            [UMessage didReceiveRemoteNotification:userInfo];
        }
        completionHandler(UIBackgroundFetchResultNewData);
    }
    //iOS10新增：处理前台收到通知的代理方法
    -(void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler{
        NSDictionary * userInfo = notification.request.content.userInfo;
        if([notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
            [UMessage setAutoAlert:NO];
            //应用处于前台时的远程推送接受
            //必须加这句代码
            [UMessage didReceiveRemoteNotification:userInfo];
        }else{
            //应用处于前台时的本地推送接受
        }
        completionHandler(UNNotificationPresentationOptionSound|UNNotificationPresentationOptionBadge|UNNotificationPresentationOptionAlert);
    }
    //iOS10新增：处理后台点击通知的代理方法
    -(void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler{
        NSDictionary * userInfo = response.notification.request.content.userInfo;
        if([response.notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
            //应用处于后台时的远程推送接受
            //必须加这句代码
            [UMessage didReceiveRemoteNotification:userInfo];
        }else{
            //应用处于后台时的本地推送接受
        }
    }

    //获取deviceToken
    - (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
    {
        [UMPushModule application:application didRegisterDeviceToken:deviceToken];
    }
    `;
    
    var windowMakeKeyAndVisible = appDelegateContents.match(/(\[self.window makeKeyAndVisible\];)/g);

    if (!windowMakeKeyAndVisible) {
        console.log('Couldn\'t find jsCodeLocation setting in AppDelegate.');
    }
    var newJsCodeLocationAssignmentStatement = "[RNUMConfigure init];";
    if (~appDelegateContents.indexOf(newJsCodeLocationAssignmentStatement)) {
        console.log(`"jsCodeLocation" already pointing to "[CodePush bundleURL]".`);
    } else {
        if (windowMakeKeyAndVisible.length === 1) {
            var oldJsCodeAssignmentStatement = windowMakeKeyAndVisible[0];
            var jsCodeLocationPatch = `${oldJsCodeAssignmentStatement}

                    ${configUmeng}
            `;
            appDelegateContents = appDelegateContents.replace(oldJsCodeAssignmentStatement,
                      jsCodeLocationPatch);
            
        } else {
            console.log(`AppDelegate isn't compatible for linking`);
        }
    }


    var plistPath = getPlistPath();

    if (!plistPath) {
        return Promise.reject(`Couldn't find .plist file. You might need to update it manually \
    Please refer to plugin configuration section for iOS at \
    https://github.com/astrocean/react-native-umeng-push#iOS`);
    }

    var plistContents = fs.readFileSync(plistPath, "utf8");

    // 3. Add UmengAppKey to plist file
    var parsedInfoPlist = plist.parse(plistContents);
    if (parsedInfoPlist.UmengAppKey) {
        console.log(`"UmengAppKey" already specified in the plist file.`);
        writePatches();
    } else {
        inquirer.prompt({
            "type": "input",
            "name": "iosUmengAppKey",
            "message": "What is your Umeng app key for iOS (hit <ENTER> to ignore)"
        }).then(function(answer) {
            parsedInfoPlist.UmengAppKey = answer.iosUmengAppKey || "umeng-app-key-here";
            plistContents = plist.build(parsedInfoPlist);

            writePatches();
        });
    }

   if (parsedInfoPlist.UmengAppChannel) {
      console.log(`"UmengAppChannel" already specified in the plist file.`);
      writePatches();
      return Promise.resolve();
  } else {
      return inquirer.prompt({
          "type": "input",
          "name": "iosUmengAppChannel",
          "message": "What is your Umeng Channel for iOS (hit <ENTER> to ignore)"
      }).then(function(answer) {
          parsedInfoPlist.UmengAppChannel = answer.iosUmengAppChannel || "DEFAULT_CHANNEL_iOS";
          plistContents = plist.build(parsedInfoPlist);

          writePatches();
          return Promise.resolve();
      });
  }

    function writePatches() {
        fs.writeFileSync(appDelegatePath, appDelegateContents);
        fs.writeFileSync(plistPath, plistContents);
    }

    // Helper that filters an array with AppDelegate.m paths for a path with the app name inside it
    // Should cover nearly all cases
    function findFileByAppName(array, appName) {
        if (array.length === 0 || !appName) return null;

        for (var i = 0; i < array.length; i++) {
            var path = array[i];
            if (path && path.indexOf(appName) !== -1) {
                return path;
            }
        }

        return null;
    }

    function getDefaultPlistPath() {
        //this is old logic in case we are unable to find PLIST from xcode/pbxproj - at least we can fallback to default solution
        return glob.sync(`**/${package.name}/*Info.plist`, ignoreNodeModules)[0];
    }

    // This is enhanced version of standard implementation of xcode 'getBuildProperty' function
    // but allows us to narrow results by PRODUCT_NAME property also.
    // So we suppose that proj name should be the same as package name, otherwise fallback to default plist path searching logic
    function getBuildSettingsPropertyMatchingTargetProductName(parsedXCodeProj, prop, targetProductName, build){
        var target;
        var COMMENT_KEY = /_comment$/;
        var PRODUCT_NAME_PROJECT_KEY = 'PRODUCT_NAME';
        var TV_OS_DEPLOYMENT_TARGET_PROPERTY_NAME = 'TVOS_DEPLOYMENT_TARGET';
        var TEST_HOST_PROPERTY_NAME = 'TEST_HOST';

        var configs = parsedXCodeProj.pbxXCBuildConfigurationSection();
        for (var configName in configs) {
            if (!COMMENT_KEY.test(configName)) {
                var config = configs[configName];
                if ( (build && config.name === build) || (build === undefined) ) {
                    if (targetProductName) {
                        if (config.buildSettings[prop] !== undefined && config.buildSettings[PRODUCT_NAME_PROJECT_KEY] == targetProductName) {
                            target = config.buildSettings[prop];
                        }       
                    } else {
                        if (config.buildSettings[prop] !== undefined  &&
                        //exclude tvOS projects
                        config.buildSettings[TV_OS_DEPLOYMENT_TARGET_PROPERTY_NAME] == undefined &&
                        //exclude test app
                        config.buildSettings[TEST_HOST_PROPERTY_NAME] == undefined) {
                            target = config.buildSettings[prop];
                        }
                    }              
                }
            }
        }
        return target;
    }

    function getPlistPath(){
        var xcodeProjectPaths = glob.sync(`**/*.xcodeproj/project.pbxproj`, ignoreNodeModulesAndPods);
        if (!xcodeProjectPaths){
            return getDefaultPlistPath();
        }

        if (xcodeProjectPaths.length !== 1) {
            console.log('Could not determine correct xcode proj path to retrieve related plist file, there are multiple xcodeproj under the solution.');
            return getDefaultPlistPath();
        }

        var xcodeProjectPath = xcodeProjectPaths[0];
        var parsedXCodeProj;

        try {
            var proj = xcode.project(xcodeProjectPath);
            //use sync version because there are some problems with async version of xcode lib as of current version
            parsedXCodeProj = proj.parseSync();
        }
        catch(e) {
            console.log('Couldn\'t read info.plist path from xcode project - error: ' + e.message);
            return getDefaultPlistPath();
        }

        var INFO_PLIST_PROJECT_KEY = 'INFOPLIST_FILE';
        var RELEASE_BUILD_PROPERTY_NAME = "Release";
        var targetProductName = package ? package.name : null;

        //Try to get 'Release' build of ProductName matching the package name first and if it doesn't exist then try to get any other if existing
        var plistPathValue = getBuildSettingsPropertyMatchingTargetProductName(parsedXCodeProj, INFO_PLIST_PROJECT_KEY, targetProductName, RELEASE_BUILD_PROPERTY_NAME) ||
            getBuildSettingsPropertyMatchingTargetProductName(parsedXCodeProj, INFO_PLIST_PROJECT_KEY, targetProductName) ||
            getBuildSettingsPropertyMatchingTargetProductName(parsedXCodeProj, INFO_PLIST_PROJECT_KEY, null, RELEASE_BUILD_PROPERTY_NAME) ||
            getBuildSettingsPropertyMatchingTargetProductName(parsedXCodeProj, INFO_PLIST_PROJECT_KEY) ||
            parsedXCodeProj.getBuildProperty(INFO_PLIST_PROJECT_KEY, RELEASE_BUILD_PROPERTY_NAME) ||
            parsedXCodeProj.getBuildProperty(INFO_PLIST_PROJECT_KEY);

        if (!plistPathValue){
            return getDefaultPlistPath();
        }

        //also remove surrounding quotes from plistPathValue to get correct path resolved
        //(see https://github.com/Microsoft/react-native-code-push/issues/534#issuecomment-302069326 for details)
        return path.resolve(path.dirname(xcodeProjectPath), '..', plistPathValue.replace(/^"(.*)"$/, '$1'));
    }
}



