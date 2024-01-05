
var nooperation = function () { };

module.exports = {
    checkPermission: function (successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'checkPermission', []);
    },
    startOverApp: function (options,successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'open', [ options ]);
    },
    closeOverApp: function (callback) {
        cordova.exec(
            function () {
                // Execute the callback when the service is closed
                if (typeof callback === 'function') {
                    callback();
                }
            },
            nooperation,
            'FloatOver',
            'close',
            []
        );
    }
    //closeOverApp: function () {
        //cordova.exec(nooperation, nooperation, 'FloatOver', 'close', []);
    //}
};
