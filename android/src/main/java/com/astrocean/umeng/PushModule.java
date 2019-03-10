package com.astrocean.umeng;

import android.app.Activity;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.common.UmengMessageDeviceConfig;
import com.umeng.message.common.inter.ITagManager;
import com.umeng.message.tag.TagManager;
import com.umeng.message.IUmengRegisterCallback;


import org.android.agoo.xiaomi.MiPushRegistar;
import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.meizu.MeizuRegister;

/**
 * Created by wangfei on 17/8/30
 */

public class PushModule extends ReactContextBaseJavaModule {
    private final int SUCCESS = 200;
    private final int ERROR = 0;
    private final int CANCEL = -1;
    private static final String TAG = PushModule.class.getSimpleName();
    private static Handler mSDKHandler = new Handler(Looper.getMainLooper());
    private ReactApplicationContext context;
    private boolean isGameInited = false;
    private String deviceToken = "";
    private String registerState = "未注册";
    private static Activity ma;
    private PushAgent mPushAgent;
    private Handler handler;
    private static PushModule pushModule = null;

    public static String XIAOMI_ID=null;
    public static String XIAOMI_KEY=null;

    public PushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        //获取消息推送代理示例
        mPushAgent = PushAgent.getInstance(context);
        pushModule=this;
    }

    public PushAgent getMPushAgent(){
        return this.mPushAgent;
    }
    public ReactApplicationContext getContext(){
        return this.context;
    }

    public void setDeviceToken(String deviceToken){
       this.deviceToken=deviceToken;
    }

    public void setRegisterState(String state){
       this.registerState=state;
    }

    public static void initPushSDK(Activity activity) {
        ma = activity;
    }

    public static void register() {
        if(pushModule==null){
            return;
        }
        pushModule.setRegisterState("准备注册");

        // 小米对后台进程做了诸多限制。若使用一键清理，应用的channel进程被清除，将接收不到推送。为了增加推送的送达率，可选择接入小米托管弹窗功能。通知将由小米系统托管弹出，点击通知栏将跳转到指定的Activity。该Activity需继承自UmengNotifyClickActivity，同时实现父类的onMessage方法，对该方法的intent参数进一步解析即可，该方法异步调用，不阻塞主线程。
        //暂未处理
        //         仅在小米MIUI设备上生效。
        // 集成小米push的版本暂不支持多包名。
        // MiPushRegistar.register(final Context context, final String XIAOMI_ID, final String XIAOMI_KEY);
        if(PushModule.XIAOMI_ID!=null){
           MiPushRegistar.register(pushModule.getContext(),PushModule.XIAOMI_ID,PushModule.XIAOMI_KEY);
        }
        
        //         仅在华为EMUI设备上生效。
        // 集成华为Push的版本暂不支持多包名。
        // 若使用华为Push通道，则app的targetSdkVersion必须设置为25或25以下，设置为26及以上，会导致EMUI 8.0设备无法弹出通知。
        // HuaWeiRegister.register(final Context context);
        HuaWeiRegister.register(pushModule.getContext());

       
        
        //该方法是【友盟+】Push后台进行日活统计及多维度推送的必调用方法，请务必调用！
        //在所有的Activity 的onCreate 方法或在应用的BaseActivity的onCreate方法中添加：
        pushModule.getMPushAgent().onAppStart();
        
        //通知响铃、震动及呼吸灯控制
        // mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER); //服务端控制声音
        // mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATIONPLAYSDKENABLE);//客户端允许呼吸灯点亮
        // mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATIONPLAYSDKDISABLE);//客户端禁止振动
        pushModule.getMPushAgent().setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);

        //注册推送服务，每次调用register方法都会回调该接口
        pushModule.getMPushAgent().register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                pushModule.setRegisterState("");
                pushModule.setDeviceToken(deviceToken);
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                Log.i(TAG,"注册成功：deviceToken：-------->  " + deviceToken);
            }
            @Override
            public void onFailure(String s, String s1) {
                pushModule.setRegisterState("注册失败：-------->  " + "s:" + s + ",s1:" + s1);
                Log.e(TAG,"注册失败：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });
    }

    @Override
    public String getName() {
        return "UMPushModule";
    }
    private static void runOnMainThread(Runnable runnable) {
        mSDKHandler.postDelayed(runnable, 0);
    }
     
    
    
    @ReactMethod
    public void getDeviceTocken(final Callback successCallback) {
        successCallback.invoke(registerState,deviceToken);
    }

    
    @ReactMethod
    public void addTag(String tag, final Callback successCallback) {
        mPushAgent.getTagManager().addTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final ITagManager.Result result) {


                        if (isSuccess) {
                            successCallback.invoke(SUCCESS,result.remain);
                        } else {
                            successCallback.invoke(ERROR,0);
                        }
            }
        }, tag);
    }

    @ReactMethod
    public void deleteTag(String tag, final Callback successCallback) {
        mPushAgent.getTagManager().deleteTags(new TagManager.TCallBack() {
            @Override
            public void onMessage(boolean isSuccess, final ITagManager.Result result) {
                Log.i(TAG, "isSuccess:" + isSuccess);
                if (isSuccess) {
                    successCallback.invoke(SUCCESS,result.remain);
                } else {
                    successCallback.invoke(ERROR,0);
                }
            }
        }, tag);
    }

    @ReactMethod
    public void listTag(final Callback successCallback) {
        mPushAgent.getTagManager().getTags(new TagManager.TagListCallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final List<String> result) {
                mSDKHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess) {
                            if (result != null) {

                                successCallback.invoke(SUCCESS,resultToList(result));
                            } else {
                                successCallback.invoke(ERROR,resultToList(result));
                            }
                        } else {
                            successCallback.invoke(ERROR,resultToList(result));
                        }

                    }
                });

            }
        });
    }

    @ReactMethod
    public void addAlias(String alias, String aliasType, final Callback successCallback) {
        mPushAgent.addAlias(alias, aliasType, new UTrack.ICallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final String message) {
                Log.i(TAG, "isSuccess:" + isSuccess + "," + message);

                        Log.e("xxxxxx","isuccess"+isSuccess);
                        if (isSuccess) {
                            successCallback.invoke(SUCCESS);
                        } else {
                            successCallback.invoke(ERROR);
                        }


            }
        });
    }

    @ReactMethod
    public void addAliasType() {
        if(ma==null){
            return;
        }
        Toast.makeText(ma,"function will come soon",Toast.LENGTH_LONG);
    }

    @ReactMethod
    public void addExclusiveAlias(String exclusiveAlias, String aliasType, final Callback successCallback) {
        mPushAgent.setAlias(exclusiveAlias, aliasType, new UTrack.ICallBack() {
            @Override
            public void onMessage(final boolean isSuccess, final String message) {

                        Log.i(TAG, "isSuccess:" + isSuccess + "," + message);
                        if (Boolean.TRUE.equals(isSuccess)) {
                            successCallback.invoke(SUCCESS);
                        }else {
                            successCallback.invoke(ERROR);
                        }



            }
        });
    }

    @ReactMethod
    public void deleteAlias(String alias, String aliasType, final Callback successCallback) {
        mPushAgent.deleteAlias(alias, aliasType, new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean isSuccess, String s) {
                if (Boolean.TRUE.equals(isSuccess)) {
                    successCallback.invoke(SUCCESS);
                }else {
                    successCallback.invoke(ERROR);
                }
            }
        });
    }

    @ReactMethod
    public void appInfo(final Callback successCallback) {
        String pkgName = context.getPackageName();
        String info = String.format("DeviceToken:%s\n" + "SdkVersion:%s\nAppVersionCode:%s\nAppVersionName:%s",
            mPushAgent.getRegistrationId(), MsgConstant.SDK_VERSION,
            UmengMessageDeviceConfig.getAppVersionCode(context), UmengMessageDeviceConfig.getAppVersionName(context));
        successCallback.invoke("应用包名:" + pkgName + "\n" + info);
    }
    private WritableMap resultToMap(ITagManager.Result result){
        WritableMap map = Arguments.createMap();
        if (result!=null){
            map.putString("status",result.status);
            map.putInt("remain",result.remain);
            map.putString("interval",result.interval+"");
            map.putString("errors",result.errors);
            map.putString("last_requestTime",result.last_requestTime+"");
            map.putString("jsonString",result.jsonString);
        }
        return map;
    }
    private WritableArray resultToList(List<String> result){
        WritableArray list = Arguments.createArray();
        if (result!=null){
            for (String key:result){
                list.pushString(key);
            }
        }
        Log.e("xxxxxx","list="+list);
        return list;
    }
}