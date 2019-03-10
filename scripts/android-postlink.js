var fs = require("fs");
var glob = require("glob");

module.exports = () => {

    console.log("Running android postlink script");

    var ignoreFolders = { ignore: ["node_modules/**", "**/build/**"] };
    var manifestPath = glob.sync("**/AndroidManifest.xml", ignoreFolders)[0];

    function findMainApplication() {
        if (!manifestPath) {
            return null;
        }

        var manifest = fs.readFileSync(manifestPath, "utf8");

        // Android manifest must include single 'application' element
        var matchResult = manifest.match(/application\s+android:name\s*=\s*"(.*?)"/);
        if (matchResult) {
            var appName = matchResult[1];
        } else {
            return null;
        }
        
        var nameParts = appName.split('.');
        var searchPath = glob.sync("**/" + nameParts[nameParts.length - 1] + ".java", ignoreFolders)[0];
        return searchPath;
    }

    var mainApplicationPath = findMainApplication() || glob.sync("**/MainApplication.java", ignoreFolders)[0];

    // 1. Add the Config Umeng
    var configInit = `
      //可不传 channel:string,默认为 ：DEFAULT_CHANNEL_ANDROID
      //可不传 type: int,默认为：UMConfigure.DEVICE_TYPE_PHONE，可通过 DplusReactPackage.UMCONFIG.DEVICE_TYPE_XXX 设置值
      DplusReactPackage.ConfigInit( this,"<appKey>","<pushSecret>",<channel>,<type>);
    `;

    function isAlreadyOverridden(codeContents) {
        return /DplusReactPackage.ConfigInit/.test(codeContents);
    }

    if (mainApplicationPath) {
        var mainApplicationContents = fs.readFileSync(mainApplicationPath, "utf8");
        if (isAlreadyOverridden(mainApplicationContents)) {
            console.log(`"DplusReactPackage.ConfigInit" is already write`);
        } else {
            var soLoaderInitFunction = "SoLoader.init(this, /* native exopackage */ false);";
            mainApplicationContents = mainApplicationContents.replace(soLoaderInitFunction,
                `${soLoaderInitFunction}\n${configInit}`);
            fs.writeFileSync(mainApplicationPath, mainApplicationContents);
        }
    } else {
      return Promise.reject(`Couldn't find Android application entry point. You might need to update it manually. \
      Please refer to plugin configuration section for Android at \
      https://github.com/astrocean/react-native-umeng-push#Android for more details`);
    }

    return Promise.resolve();
}