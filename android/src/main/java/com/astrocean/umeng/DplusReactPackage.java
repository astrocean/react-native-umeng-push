package com.astrocean.umeng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.react.uimanager.ViewManager;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by wangfei on 17/8/28.
 */

public class DplusReactPackage implements ReactPackage {

    public static final UMConfigure UMCONFIG = UMConfigure;
    public static final String DEFAULT_CHANNEL = "DEFAULT_CHANNEL_ANDROID";
    public static final Int DEFAULT_DEVICE_TYPE = UMConfigure.DEVICE_TYPE_PHONE;

    // 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
    // 参数一：当前上下文context；
    // 参数二：应用申请的Appkey（需替换）；
    // 参数三：渠道名称；
    // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
    // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
    // UMConfigure.init(this, "替换为Appkey,服务后台位置：应用管理 -> 应用信息 -> Appkey", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "替换为秘钥信息,服务后台位置：应用管理 -> 应用信息 -> Umeng Message Secret");
    public static void InitConfigure(Context context,String appKey,String pushSecret,String channel,int type){
        RNUMConfigure.init(context, appKey,_channel,type,pushSecret);
        //push注册
        PushModule.register();
    }
    public static void ConfigXiaoMi(String xiaoMIID,String xiaoMiKey){
        PushModule.XIAOMI_ID=xiaoMIID;
        PushModule.XIAOMI_KEY=xiaoMiKey;
    }
    public static void ConfigInit(Context context,String appKey,String pushSecret) {
        DplusReactPackage.InitConfigure(context, appKey, DplusReactPackage.DEFAULT_CHANNEL, DplusReactPackage.DEFAULT_DEVICE_TYPE,pushSecret);
    }
    public static void ConfigInit(Context context,String appKey,String pushSecret,String channel) {
        String _channel=channel;
        if(channel==null){
            _channel=DplusReactPackage.DEFAULT_CHANNEL;
        }
        DplusReactPackage.InitConfigure(context, appKey,_channel, UMConfigure.DEVICE_TYPE_PHONE,pushSecret);
    }
    public static void ConfigInit(Context context,String appKey,String pushSecret,String channel,int type) {
        String _channel=channel;
        if(channel==null){
            _channel=DplusReactPackage.DEFAULT_CHANNEL;
        }
        DplusReactPackage.InitConfigure(context, appKey,_channel,type,pushSecret);
    }

    
     
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    /**
     * 如需要添加本地方法，只需在这里add
     *
     * @param reactContext
     * @return
     */
    @Override
    public List<NativeModule> createNativeModules(
        ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        // modules.add(new ShareModule(reactContext));
        modules.add(new PushModule(reactContext));
        // modules.add(new AnalyticsModule(reactContext));
        return modules;
    }
}