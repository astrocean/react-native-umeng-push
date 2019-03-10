import { NativeModules } from 'react-native';

const { UMPushModule } = NativeModules;

const UmengPush={
  UMPushModule,
  getDeviceTocken:(handler: Function)=>{
    UMPushModule.getDeviceToken((state:String,deviceToken:String)=>{
      handler&&handler(state,deviceToken);
    });
  }
}

export default UmengPush;
