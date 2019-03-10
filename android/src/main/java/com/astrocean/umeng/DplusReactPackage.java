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

    // RNUMConfture.init接口一共五个参数，其中第一个参数为Context，第二个参数为友盟Appkey，第三个参数为channel，第四个参数为应用类型（手机或平板），第五个参数为push的secret（如果没有使用push，可以为空）。
    public static void ConfigInit(Context context,String appKey,String pushSecret) {
        RNUMConfigure.init(context, appKey, DplusReactPackage.DEFAULT_CHANNEL, DplusReactPackage.DEFAULT_DEVICE_TYPE,pushSecret);
    }
    public static void ConfigInit(Context context,String appKey,String pushSecret,String channel) {
        String _channel=channel;
        if(channel==null){
            _channel=DplusReactPackage.DEFAULT_CHANNEL;
        }
        RNUMConfigure.init(context, appKey,_channel, UMConfigure.DEVICE_TYPE_PHONE,pushSecret);
    }
    public static void ConfigInit(Context context,String appKey,String pushSecret,String channel,int type) {
        String _channel=channel;
        if(channel==null){
            _channel=DplusReactPackage.DEFAULT_CHANNEL;
        }
        RNUMConfigure.init(context, appKey,_channel,type,pushSecret);
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