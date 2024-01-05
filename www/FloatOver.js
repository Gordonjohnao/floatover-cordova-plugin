
var nooperation = function () { };

module.exports = {
    checkPermission: function (successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'checkPermission', []);
    },
    startOverApp: function (options,successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'open', [ options ]);
    },
    //closeOverApp: function () {
        //cordova.exec(nooperation, nooperation, 'FloatOver', 'close', []);
    //}
    closeOverApp: function () {
    console.log('Calling closeOverApp');
    cordova.exec(
        function () { console.log('Success callback'); },
        function (error) { console.error('Error callback:', error); },
        'FloatOver',
        'close',
        []
    );
}

};
