import { NativeModules } from 'react-native';

const { UMPushModule } = NativeModules;

const UmengPush={
  UMPushModule,
  getDeviceToken:(handler: Function)=>{
    UMPushModule.getDeviceToken((state:String,deviceToken:String)=>{
      handler&&handler(state,deviceToken);
    });
  }
}


module.exports = UmengPush;
