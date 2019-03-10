//
//  PushModule.m
//  UMComponent
//
//  Created by wyq.Cloudayc on 11/09/2017.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "UMPushModule.h"
#import <UMPush/UMessage.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>

@implementation UMPushModule

RCT_EXPORT_MODULE();

@property (nonatomic, copy) NSString *deviceToken;


static UMPushModule* _instance = nil;
 
+(instancetype) shareInstance
{
    static dispatch_once_t onceToken ;
    dispatch_once(&onceToken, ^{
        _instance = [[super allocWithZone:NULL] init] ;
    }) ;
    
    return _instance ;
}
 
+(id) allocWithZone:(struct _NSZone *)zone
{
    return [UMPushModule shareInstance] ;
}
 
-(id) copyWithZone:(struct _NSZone *)zone
{
    return [UMPushModule shareInstance] ;
}
 

//  /**未知错误*/
//  kUMessageErrorUnknown = 0,
//  /**响应出错*/
//  kUMessageErrorResponseErr = 1,
//  /**操作失败*/
//  kUMessageErrorOperateErr = 2,
//  /**参数非法*/
//  kUMessageErrorParamErr = 3,
//  /**条件不足(如:还未获取device_token，添加tag是不成功的)*/
//  kUMessageErrorDependsErr = 4,
//  /**服务器限定操作*/
//  kUMessageErrorServerSetErr = 5,
- (NSString *)checkErrorMessage:(NSInteger)code
{
  switch (code) {
    case 1:
      return @"响应出错";
      break;
    case 2:
      return @"操作失败";
      break;
    case 3:
      return @"参数非法";
      break;
    case 4:
      return @"条件不足(如:还未获取device_token，添加tag是不成功的)";
      break;
    case 5:
      return @"服务器限定操作";
      break;
    default:
      break;
  }
  return nil;
}

- (void)handleResponse:(id  _Nonnull)responseObject remain:(NSInteger)remain error:(NSError * _Nonnull)error completion:(RCTResponseSenderBlock)completion
{
  if (completion) {
    if (error) {
      NSString *msg = [self checkErrorMessage:error.code];
      if (msg.length == 0) {
        msg = error.localizedDescription;
      }
      completion(@[@(error.code), @(remain)]);
    } else {
      if ([responseObject isKindOfClass:[NSDictionary class]]) {
        NSDictionary *retDict = responseObject;
        if ([retDict[@"success"] isEqualToString:@"ok"]) {
          completion(@[@200, @(remain)]);
        } else {
          completion(@[@(-1), @(remain)]);
        }
      } else {
        completion(@[@(-1), @(remain)]);
      }
      
    }
  }
}

- (void)handleGetTagResponse:(NSSet * _Nonnull)responseTags remain:(NSInteger)remain error:(NSError * _Nonnull)error completion:(RCTResponseSenderBlock)completion
{
  if (completion) {
    if (error) {
      NSString *msg = [self checkErrorMessage:error.code];
      if (msg.length == 0) {
        msg = error.localizedDescription;
      }
      completion(@[@(error.code), @(remain), @[]]);
    } else {
      if ([responseTags isKindOfClass:[NSSet class]]) {
        NSArray *retList = responseTags.allObjects;
        completion(@[@200, @(remain), retList]);
      } else {
        completion(@[@(-1), @(remain), @[]]);
      }
    }
  }
}
- (void)handleAliasResponse:(id  _Nonnull)responseObject error:(NSError * _Nonnull)error completion:(RCTResponseSenderBlock)completion
{
  if (completion) {
    if (error) {
      NSString *msg = [self checkErrorMessage:error.code];
      if (msg.length == 0) {
        msg = error.localizedDescription;
      }
      completion(@[@(error.code)]);
    } else {
      if ([responseObject isKindOfClass:[NSDictionary class]]) {
        NSDictionary *retDict = responseObject;
        if ([retDict[@"success"] isEqualToString:@"ok"]) {
          completion(@[@200]);
        } else {
          completion(@[@(-1)]);
        }
      } else {
        completion(@[@(-1)]);
      }
      
    }
  }
}

// + (void)init2
// {
    
//     // Push功能配置
//     UMessageRegisterEntity * entity = [[UMessageRegisterEntity alloc] init];
//     entity.types = UMessageAuthorizationOptionBadge|UMessageAuthorizationOptionAlert|UMessageAuthorizationOptionSound;
//     //如果你期望使用交互式(只有iOS 8.0及以上有)的通知，请参考下面注释部分的初始化代码
//     if (([[[UIDevice currentDevice] systemVersion]intValue]>=8)&&([[[UIDevice currentDevice] systemVersion]intValue]<10)) {
//             UIMutableUserNotificationAction *action1 = [[UIMutableUserNotificationAction alloc] init];
//             action1.identifier = @"action1_identifier";
//             action1.title=@"打开应用";
//             action1.activationMode = UIUserNotificationActivationModeForeground;//当点击的时候启动程序
//             UIMutableUserNotificationAction *action2 = [[UIMutableUserNotificationAction alloc] init];  //第二按钮
//             action2.identifier = @"action2_identifier";
//             action2.title=@"忽略";
//             action2.activationMode = UIUserNotificationActivationModeBackground;//当点击的时候不启动程序，在后台处理
//             action2.authenticationRequired = YES;//需要解锁才能处理，如果action.activationMode = UIUserNotificationActivationModeForeground;则这个属性被忽略；
//             action2.destructive = YES;
//             UIMutableUserNotificationCategory *actionCategory1 = [[UIMutableUserNotificationCategory alloc] init];
//             actionCategory1.identifier = @"category1";//这组动作的唯一标示
//             [actionCategory1 setActions:@[action1,action2] forContext:(UIUserNotificationActionContextDefault)];
//             NSSet *categories = [NSSet setWithObjects:actionCategory1, nil];
//             entity.categories=categories;
//         }
//         //如果要在iOS10显示交互式的通知，必须注意实现以下代码
//         if ([[[UIDevice currentDevice] systemVersion]intValue]>=10) {
//             UNNotificationAction *action1_ios10 = [UNNotificationAction actionWithIdentifier:@"action1_identifier" title:@"打开应用" options:UNNotificationActionOptionForeground];
//             UNNotificationAction *action2_ios10 = [UNNotificationAction actionWithIdentifier:@"action2_identifier" title:@"忽略" options:UNNotificationActionOptionForeground];
//             //UNNotificationCategoryOptionNone
//             //UNNotificationCategoryOptionCustomDismissAction  清除通知被触发会走通知的代理方法
//             //UNNotificationCategoryOptionAllowInCarPlay       适用于行车模式
//             UNNotificationCategory *category1_ios10 = [UNNotificationCategory categoryWithIdentifier:@"category1" actions:@[action1_ios10,action2_ios10]   intentIdentifiers:@[] options:UNNotificationCategoryOptionCustomDismissAction];
//             NSSet *categories = [NSSet setWithObjects:category1_ios10, nil];
//             entity.categories=categories;
//         }
//     [UNUserNotificationCenter currentNotificationCenter].delegate=self;
//     [UMessage registerForRemoteNotificationsWithLaunchOptions:launchOptions Entity:entity completionHandler:^(BOOL granted, NSError * _Nullable error) {
//         if (granted) {
//         }else{
//         }
//     }];
// }

+ (void)application:(UIApplication *)application didRegisterDeviceToken:(NSData *)deviceToken {
    [UMPushModule sharedInstance].deviceToken = [[[[deviceToken description] stringByReplacingOccurrencesOfString: @"<" withString: @""]
      stringByReplacingOccurrencesOfString: @">" withString: @""]
      stringByReplacingOccurrencesOfString: @" " withString: @""];
}

RCT_EXPORT_METHOD(getDeviceToken:(RCTResponseSenderBlock)callback) {
    NSString *deviceToken = self.deviceToken;
    if(deviceToken == nil) {
        deviceToken = @"";
    }
    callback(@[deviceToken]);
}

RCT_EXPORT_METHOD(addTag:(NSString *)tag response:(RCTResponseSenderBlock)completion)
{
  [UMessage addTags:tag response:^(id  _Nonnull responseObject, NSInteger remain, NSError * _Nonnull error) {
    [self handleResponse:responseObject remain:remain error:error completion:completion];
  }];
}

RCT_EXPORT_METHOD(deleteTag:(NSString *)tag response:(RCTResponseSenderBlock)completion)
{
  [UMessage deleteTags:tag response:^(id  _Nonnull responseObject, NSInteger remain, NSError * _Nonnull error) {
    [self handleResponse:responseObject remain:remain error:error completion:completion];
  }];
}

RCT_EXPORT_METHOD(listTag:(RCTResponseSenderBlock)completion)
{
  [UMessage getTags:^(NSSet * _Nonnull responseTags, NSInteger remain, NSError * _Nonnull error) {
    [self handleGetTagResponse:responseTags remain:remain error:error completion:completion];
  }];
}

RCT_EXPORT_METHOD(addAlias:(NSString *)name type:(NSString *)type response:(RCTResponseSenderBlock)completion)
{
  [UMessage addAlias:name type:type response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {
    [self handleAliasResponse:responseObject error:error completion:completion];
  }];
}

RCT_EXPORT_METHOD(addExclusiveAlias:(NSString *)name type:(NSString *)type response:(RCTResponseSenderBlock)completion)
{
  [UMessage setAlias:name type:type response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {
    [self handleAliasResponse:responseObject error:error completion:completion];
  }];
}

RCT_EXPORT_METHOD(deleteAlias:(NSString *)name type:(NSString *)type response:(RCTResponseSenderBlock)completion)
{
  [UMessage removeAlias:name type:type response:^(id  _Nonnull responseObject, NSError * _Nonnull error) {
    [self handleAliasResponse:responseObject error:error completion:completion];
  }];
}



@end
