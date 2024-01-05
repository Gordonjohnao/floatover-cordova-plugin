
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
            console.log('closeOverApp executed successfully');
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
