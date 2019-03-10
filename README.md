# react-native-umeng-push

## Getting started

`$ npm install react-native-umeng-push --save`

### Mostly automatic installation

`$ react-native link react-native-umeng-push`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-umeng-push` and add `RNUmengPush.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNUmengPush.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.RNUmengPushPackage;` to the imports at the top of the file
  - Add `new RNUmengPushPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-umeng-push'
  	project(':react-native-umeng-push').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-umeng-push/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-umeng-push')
  	```


## Usage
```javascript
import RNUmengPush from 'react-native-umeng-push';

// TODO: What to do with the module?
RNUmengPush;
```
