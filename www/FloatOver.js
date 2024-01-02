
var nooperation = function () { };

module.exports = {
    checkPermission: function (successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'checkPermission', []);
    },
    startOverApp: function (options,successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'open', [ options ]);
    },
    closeOverApp: function () {
        cordova.exec(nooperation, nooperation, 'FloatOver', 'close', []);
    }
    closeFloatApp: function () {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'close', []);
    }
};
