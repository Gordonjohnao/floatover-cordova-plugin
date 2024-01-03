
var nooperation = function () { };

module.exports = {
    checkPermission: function (successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'checkPermission', []);
    },
    startOverApp: function (options,successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'FloatOver', 'open', [ options ]);
    },
   // closeOverApp: function () {
        //cordova.exec(nooperation, nooperation, 'FloatOver', 'close', []);
    //}
    closeOverApp: function () {
    cordova.exec(
        function() {
            console.log('Plugin closed successfully.');
        },
        function(error) {
            console.error('Error while closing plugin:', error);
        },
        'FloatOver', 'close', []
    );
}

   
};
